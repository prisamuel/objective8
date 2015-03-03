(ns objective8.storage.storage
  (:require [korma.core :as korma]
            [objective8.storage.mappings :as mappings]))

(defn insert
  "Wrapper around Korma's insert call"
  [entity data]
  (korma/insert entity (korma/values data)))

(defn pg-store!
  "Transform a map according to its :entity value and save it in the database"
  [{:keys [entity] :as m}]
  (if-let [ent (mappings/get-mapping m)]
    (insert ent m)
    (throw (Exception. (str "Could not find database mapping for " entity)))))

(defn select
  "Wrapper around Korma's select call"
  [entity where options]
  (let [{:keys [limit]} options
        {:keys [field ordering]} (get options :sort {:field :_created_at
                                                     :ordering :ASC})
        select-query (-> (korma/select* entity)
                         (korma/where where)
                         (korma/limit limit)
                         (korma/order field ordering))
        with-relation (first (keys (:rel entity)))]
    (if with-relation
      (-> select-query
          (korma/with (mappings/get-mapping {:entity (keyword with-relation)}))
          (korma/select))
      (korma/select select-query))))

(defn- -to_
  "Replaces hyphens in keys with underscores"
  [m]
  (let [ks (keys m) vs (vals m)]
    (zipmap (map (fn [k] (-> (clojure.string/replace k #"-" "_")
                             (subs 1)
                             keyword)) ks)
            vs)))

(defn pg-retrieve
  "Retrieves objects from the database based on a query map

   - The map must include an :entity key
   - Hyphens in key words are replaced with underscores"

  ([query]
   (pg-retrieve query {}))

  ([{:keys [entity] :as query} options]
   (if entity
     (let [result (select (mappings/get-mapping query) (-to_ (dissoc query :entity)) options)]
       {:query query
        :options options
        :result result})
     (throw (Exception. "Query map requires an :entity key")))))

(defn update [entity new-fields where]
  (korma/update entity 
                (korma/set-fields new-fields) 
                (korma/where where)))

(defn pg-update-bearer-token!
  "Wrapper around Korma's update call"
  [{:keys [entity] :as m}]
  (if-let [bearer-token-entity (mappings/get-mapping m)] 
    (let [where {:bearer_name (:bearer-name m)}]
      (update bearer-token-entity m where)) 
    (throw (Exception. (str "Could not find database mapping for " entity)))))

(defn pg-update-invitation-status! [invitation-uuid new-status]
  (let [invitation-entity (mappings/get-mapping {:entity :invitation})
        updated-invitation (-> (select invitation-entity {:uuid invitation-uuid} {})
                               first
                               (assoc :status new-status))]
    (update invitation-entity updated-invitation {:uuid invitation-uuid})))
