(ns twitter-6w.core
  (require [twitter.oauth :as oauth]
           [twitter.api.search :as twitter]
           [clojure.data.codec.base64 :as b64]
           [clj-http.client :as client]
           [clojure.data.json :as json])
  (import [java.net URLEncoder]))

; Using the Clojure Twitter Client
; (def twitter-creds
;   (oauth/make-oauth-creds consumer-key
;                           consumer-secret
;                           (System/getenv "TWITTER-USER-ACCESS-TOKEN")
;                           (System/getenv "TWITTER-ACCESS-TOKEN-SECRET")))
;
; (defn make-twitter-call
;   [query]
;   (twitter/search :oauth-creds twitter-creds :params {:q query :count 100}))

;; If this were an actual project I would move Auth related things to a separate
;; module. I think the only thing the core module would be the print-tweets and main
;; function

;; Twitter API Helpers

(defn get-body
  [response]
    (json/read-str (:body response) :key-fn keyword))

;; Twitter Auth

(def consumer-key (System/getenv "TWITTER-CONSUMER-KEY"))
(def consumer-secret (System/getenv "TWITTER-CONSUMER-SECRET"))

(def credentials
  (str (URLEncoder/encode consumer-key) ":" (URLEncoder/encode consumer-secret)))

(def basic-64-encoded-credentials
  (String. (b64/encode (.getBytes credentials)) "UTF-8"))

(def token-exchange-url "https://api.twitter.com/oauth2/token")

(def bearer-token-exchange-call
  (client/post token-exchange-url
     {:body "grant_type=client_credentials"
      :headers {"Content-Type" "application/x-www-form-urlencoded;charset=UTF-8"
                "Authorization" (str "Basic " basic-64-encoded-credentials)}}))

(def bearer-header
  (str "Bearer " (:access_token (get-body bearer-token-exchange-call))))

;; Search

(defn search-url
  [query]
  (str "https://api.twitter.com/1.1/search/tweets.json?q=" query))

(defn make-search-call
  [query]
  (client/get (search-url query) {:headers {:authorization bearer-header}}))

(defn extract-tweets
  [response]
  (:statuses (get-body response)))

;; Core Functionality

(defn print-tweets
 [tweets]
  (doseq [tweet tweets]
            (prn (:text tweet))))

(defn -main [& args]
  (-> "6Wunderkinder" make-search-call extract-tweets print-tweets))
