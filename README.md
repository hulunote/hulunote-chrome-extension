# Hulunote Chrome Extension (ClojureScript)

* compile: `yarn release`

After compilation, the target files are all under `unpacked`. Use the pack plug-in in chrome and select the directory `unpacked`.


```clj
:builds {
        :extension {:target :esm
                    :output-dir "unpacked/out"
                    :runtime :custom
                    :modules {:shared {:entries []}
                              :background {:entries [hulunote-chrome-extension.background]
                                           :depends-on #{:shared}} 
                              :popup {:entries [hulunote-chrome-extension.popup]
                                      :depends-on #{:shared}}
                              :options {:entries [hulunote-chrome-extension.options]
                                        :depends-on #{:shared}}
                              ;; new code here
                              :new esm {:entries [hulunote-chrome-extension.new export code]
                                       :depends-on #{:shared}}}}
          ;; no need to change
          :core {:target :browser
                 :output-dir "unpacked/out"
                 :modules {:content-scripts {:entries [hulunote-chrome-extension.core]}}}
        }
```

```html
<script type="module" src="../out/some-script.js"></script>
```
