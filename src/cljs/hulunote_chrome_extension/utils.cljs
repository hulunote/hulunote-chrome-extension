(ns hulunote-chrome-extension.utils
  (:require [clojure.string :as strings]))

(defn get-element-by-id [id]
  (.getElementById js/document id))

(defn get-elements-by-tag-name [tag-name]
  (.getElementsByTagName js/document tag-name))

(defn add-event-listener [key func]
  (.addEventListener js/document key func))

(defn get-local-storage-value [key]
  (aget js/localStorage key))

(defn set-local-storage-value [key value]
  (aset js/localStorage key value))

(defn with-chrome-storage-value [key func]
  (.. js/chrome -storage -sync
      (get key #(let [value (if % (aget % key) nil)]
                  (func value)))))

(defn set-chrome-storage-value [key value]
  (.. js/chrome -storage -sync
      (set (clj->js {key value}))))

;; 下面的是从chrome storage取值的函数，因为不想引入async，所以这里直接嵌套 

(defn with-chrome-storage-value2 [key1 key2 func]
  (with-chrome-storage-value key1 
    (fn [value1]
      (with-chrome-storage-value key2 
        (fn [value2]
          (func value1 value2))))))

(defn with-chrome-storage-value3 [key1 key2 key3 func]
  (with-chrome-storage-value2 key1 key2
    (fn [value1 value2]
      (with-chrome-storage-value [key3]
        (fn [value3]
          (func value1 value2 value3))))))

(defn with-chrome-storage-value4 [key1 key2 key3 key4 func]
  (with-chrome-storage-value3 key1 key2 key3
    (fn [v1 v2 v3]
      (with-chrome-storage-value key4 
        (fn [v4]
          (func v1 v2 v3 v4))))))

(defn with-chrome-storage-value5 [key1 key2 key3 key4 key5 func]
  (with-chrome-storage-value4 key1 key2 key3 key4
    (fn [v1 v2 v3 v4]
      (with-chrome-storage-value key5
        (fn [v5]
          (func v1 v2 v3 v4 v5))))))

(defn send-chrome-message [type message]
  (.. js/chrome -runtime (sendMessage (clj->js {:type type
                                                :message message}))))

(defn get-chrome-tabs-selected [func]
  ;; 在v3中废弃了
  ;;(.. js/chrome -tabs (getSelected nil func))
  (.. js/chrome -tabs (query #js {:currentWindow true
                                  :active true}
                             func)))

;; v3 的background不在支持alert，这里用notification
(defn notify-message [message]
  (.. js/chrome -notifications
      (create #js {:type "basic"
                   :iconUrl "/images/logo-16.png"
                   :title "Hulunote"
                   :message message
                   :priority 1})))
