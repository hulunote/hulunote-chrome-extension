(ns hulunote-chrome-extension.popup
  (:require [hulunote-chrome-extension.utils :as u]))

(defn click-<a>-open-tab []
  (let [set-api-dom (u/get-element-by-id "set-api")
        go-to-home-dom (u/get-element-by-id "go-to-home")
        quick-enabled-dom (u/get-element-by-id "quick-enabled")]
    ;; 跳转到葫芦主页
    (set! (.-onclick go-to-home-dom)
          #(js/chrome.tabs.create #js {:active true
                                       :url (.-href go-to-home-dom)}))
    ;; 切换快速记录 
    (u/with-chrome-storage-value "quick-enabled"
                                 #(if %
                                    (set! (.-text quick-enabled-dom) "关闭快捷记录")
                                    (set! (.-text quick-enabled-dom) "开启快捷记录")))
    
    (set! (.-onclick quick-enabled-dom)
          #(u/with-chrome-storage-value "quick-enabled"
                                        (fn [value]
                                          (if value
                                            (do
                                              (u/set-chrome-storage-value "quick-enabled" false)
                                              (set! (.-text quick-enabled-dom) "开启快捷记录"))
                                            (do
                                              (u/set-chrome-storage-value "quick-enabled" true)
                                              (set! (.-text quick-enabled-dom) "关闭快捷记录")))))) 
    ))

(u/add-event-listener "DOMContentLoaded" click-<a>-open-tab)
