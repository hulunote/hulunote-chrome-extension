(ns hulunote-chrome-extension.options
  (:require [hulunote-chrome-extension.utils :as u]))

(defn save-api-action []
  (u/with-chrome-storage-value
    "api"
    (fn [value]
      (let [api (or value "")
            api-dom (u/get-element-by-id "api")
            save-dom (u/get-element-by-id "save")]
        (set! (.-value api-dom) api)
        (set! (.-onclick save-dom)
              #(do
                 (u/set-chrome-storage-value "api" (.-value api-dom))
                 (if-not (empty? (.-value api-dom))
                   (u/notify-message "保存成功")
                   (u/notify-message "请输入hulunote API地址"))))))))

(defn save-tag-action []
  (u/with-chrome-storage-value 
   "tag"
   (fn [value]
     (let [tag (or value "网页收藏")
           tag-dom (u/get-element-by-id "tag")
           save-dom (u/get-element-by-id "saveTag")]
       (set! (.-value tag-dom) tag)
       (set! (.-onclick save-dom)
             #(do
                (u/set-chrome-storage-value "tag" (.-value tag-dom))
                (if-not (empty? (.-value tag-dom))
                  (u/notify-message "保存成功")
                  (u/notify-message "请输入默认标签"))))))))

(save-api-action)
(save-tag-action)
