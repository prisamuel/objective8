(ns d-cent.storage_test
  (:require [midje.sweet :refer :all]
            [d-cent.storage :as s]))

(def test-db (atom {}))

(fact "generates ids for records"
      (let [saved-record (s/store! test-db "some-collection" {:A "B"})]
        (:_id saved-record) =not=> nil))

(fact "can store and retrieve records in collections"
      (let [saved-record-1 (s/store! test-db "some-collection" {:is "a document"})
            saved-record-2 (s/store! test-db "some-other-collection" {:is "another document"})]
        
        saved-record-1 => (contains {:is "a document"} :in-any-order) 
        saved-record-2 => (contains {:is "another document"} :in-any-order) 

        (s/retrieve test-db "some-collection" (:_id saved-record-1)) => (contains {:is "a document"} :in-any-order)
        (s/retrieve test-db "some-other-collection" (:_id saved-record-2)) => (contains {:is "another document"} :in-any-order)))

(fact "returns nil if a record doesn't exist"
      (s/store! test-db "collection" {:foo "bar"})
      (s/retrieve test-db "collection" "not an id") => nil)

(fact "returns nil if a collection doesn't exist"
      (s/retrieve test-db "does not exist" "not an id") => nil)

(fact "fetches storage atom from request map"
      (s/request->store {:d-cent {:store :the-store}}) => :the-store)

(fact "fetches based on predicate"
      (s/store! test-db "users" {:a 1})
      (s/store! test-db "users" {:a 2})
      (s/store! test-db "users" {:a 3})
      (s/find-by test-db "users" #(= 1 (:a %)))
      => (contains {:a 1}))

(fact "returns nil if fetching based on predicate returns no records"
      (s/find-by test-db "users" #(= 4 (:a %)))
      => nil)