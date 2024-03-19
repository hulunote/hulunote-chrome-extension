(ns hulunote-chrome-extension.background
  (:require [hulunote-chrome-extension.utils :as u]))

(defn send-to-hulunote [param url] 
  (.. (js/fetch url (clj->js {:method "POST"
                              :headers {"Content-Type" "application/json"}
                              :body (js/JSON.stringify (clj->js param))}))
      (then #(.json %))
      (then #(let [data (js->clj %)
                   res-message (get data "message")]
               (if (= res-message "已收藏")
                 (u/notify-message "已收藏")
                 (u/notify-message "收藏出错"))))
      (catch #(do
                (js/console.error %)
                (u/notify-message "收藏请求出错")))))

(defn send-to-hulunote-with-link [info tab]
  (u/with-chrome-storage-value2 "api" "tag"
    (fn [url tag]
      (if (empty? url)
        (u/notify-message "请填写API后才能使用哦~")
        (u/get-chrome-tabs-selected
         #(let [tag (or tag "网页收藏")
                tab (first %)
                current-url (.-url tab)
                title (.-title tab)
                selected (aget info "selectionText")
                content (str "#" tag " " selected " 来源:[" title "](" current-url ")")
                param {:content content}] 
            (send-to-hulunote param url)))))))

(defn send-to-hulunote-with-text [info tab]
  (u/with-chrome-storage-value2 "api" "tag"
    (fn [url tag]
      (if (empty? url)
        (u/notify-message "请填写API后才能使用哦~")
        (u/get-chrome-tabs-selected
         #(let [tag (or tag "网页收藏") 
                selected (aget info "selectionText")
                content (str "#" tag " " selected)
                param {:content content}]
            (send-to-hulunote param url)))))))

;; 添加菜单 - 选中文本发送至葫芦笔记
(defn add-hulunote-text-menu []
  (.. js/chrome -contextMenus
      (create #js {:id "hulunote-text"
                   :type "normal"
                   :title "选中文本发送至葫芦笔记"
                   :contexts (array "selection")})))

;; 添加菜单 - 选中文本发送至葫芦笔记(#网页收藏)
(defn add-hulunote-link-menu []
  (.. js/chrome -contextMenus
      (create #js {:id "hulunote-link"
                   :type "normal"
                   :title "选中文本发送至葫芦笔记(#网页收藏)"
                   :contexts (array "selection")})))

;; 菜单点击事件
(defn init-menu-clicked-event []
  (.. js/chrome -contextMenus -onClicked
      (addListener #(let [id (aget %1 "menuItemId")]
                      (cond
                        (= id "hulunote-text") (send-to-hulunote-with-text %1 %2)
                        (= id "hulunote-link") (send-to-hulunote-with-link %1 %2)
                        :else (u/notify-message (str "未知的菜单")))))))

;; 监听message事件 
(defn init-chrome-message-event []
  (.. js/chrome -runtime -onMessage
      (addListener (fn [message sender response]
                     (u/with-chrome-storage-value2 "api" "tag"
                       (fn [url tag]
                         (let [url (or url "")
                               message (js->clj message :keywordize-keys true)]
                           (if (empty? url)
                             (u/notify-message "请填写API后才能使用哦~")
                             (when (= (:type message) "quick-record")
                               (let [msg (:message message)
                                     tag (or tag "网页收藏")
                                     content (str "#" tag " " msg)
                                     param {:content content}]
                                 (send-to-hulunote param url)))))))))))

(do
  (add-hulunote-text-menu)
  (add-hulunote-link-menu)
  (init-menu-clicked-event)
  (init-chrome-message-event))

