(ns hulunote-chrome-extension.core
  (:require [hulunote-chrome-extension.utils :as u]))

(defn create-image []
  (let [img (js/Image.)
        style (.-style img)]
    (set! (.-src img) "https://www.hulunote.com/img/hulunote2.png")
    (set! (.-alt img) "网页收藏至葫芦笔记")
    (set! (.-title img) "将选中的文字发送到Hulunote")
    (set! (.-display style) "none")
    (set! (.-position style) "absolute")
    (set! (.-cursor style) "pointer")
    (set! (.-zIndex style) "9999999999")
    (set! (.-wdith style) "25px")
    (set! (.-height style) "25px")
    (set! (.-borderRadius style) "5px")
    (set! (.-border style) "1px solid lightgray")
    (.. js/document
        -body
        (appendChild img))
    img))

(defn select-text []
  (if (.-selection js/document)
    (.. js/document -selection (createRange) -text)
    (.. js/window (getSelection) (toString))))

(defn quick-enabled-action [val image left top]
  (when val
    (js/setTimeout (fn [] 
                     (if-not (= (select-text) "")
                       ;; 有选中文本时弹出图标
                       (js/setTimeout (fn []
                                        (set! (.. image -style -display) "block")
                                        (set! (.. image -style -left) (str left "px"))
                                        (set! (.. image -style -top) (str top "px")))
                                      100)
                       ;; 没有时不显示图标
                       (set! (.. image -style -display) "none")))
                   200)))

(defn run-on-window-load []
  (let [image (create-image)]
    ;; 鼠标的监听
    (set! (.-onmouseup js/document)
          #(let [ev (or % (.-event js/window))
                 page-x (.-pageX ev)
                 page-y (.-pageY ev) 
                 left (if (and page-x page-y) 
                        page-x
                        (+ (.-clientX ev) (.. js/document -documentElement -scrollLeft) (.. js/document -body -scrollLeft)))
                 top (if (and page-x page-y)
                       page-y
                       (+ (.-clientY ev) (.. js/document -documentElement -scrollTop) (.. js/document -body -scrollTop)))]
             (u/with-chrome-storage-value "quick-enabled" 
                                          (fn [val] (quick-enabled-action val image left top)))))
    
    ;; 点击图片
    (set! (.-onclick image)
          #(u/send-chrome-message "quick-record" (select-text)))
    
    ;; 图片鼠标松开
    (set! (.-onmouseup image)
          #(let [ev (or % (.-event js/window))]
             (set! (.-cancelBubble ev) true)))
    
    ;; document的点击
    (set! (.-onclick js/document)
          #(set! (.. image -style -display) "none"))))

(set! (.-onload js/window) run-on-window-load)
