(ns objective8.integration.front-end.admin-removals 
  (:require [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [peridot.core :as p]
            [oauth.client :as oauth]
            [objective8.http-api :as http-api]
            [objective8.config :as config]
            [objective8.utils :as utils]
            [objective8.integration.integration-helpers :as ih]))

(def USER_ID 1)
(def USER_URI (str "/users/" USER_ID))
(def OBJECTIVE_ID 234)
(def OBJECTIVE_URI (str "/objectives/" OBJECTIVE_ID))
(def TWITTER_ID "twitter-123456")

(facts "about admin-removals"
       (binding [config/enable-csrf false]
         (fact "admin can post an admin-removal for an objective"
               (against-background
                 (oauth/access-token anything anything anything) => {:user_id TWITTER_ID}
                 (http-api/find-user-by-twitter-id anything) => {:status ::http-api/success
                                                                 :result {:_id USER_ID
                                                                          :username "username"}}
                 (http-api/get-user anything) => {:result {:admin true}})
               (against-background
                 (http-api/post-admin-removal {:removal-uri OBJECTIVE_URI
                                               :removed-by-uri USER_URI}) => {:status ::http-api/success})
               (let [user-session (ih/test-context)
                     params {:removal-uri OBJECTIVE_URI}
                     {response :response} (-> user-session
                                              ih/sign-in-as-existing-user
                                              (p/request (utils/path-for :fe/post-admin-removal)
                                                         :request-method :post
                                                         :params params))]
                 (:headers response) => (ih/location-contains (utils/path-for :fe/objective-list))
                 (:status response) => 302))))
