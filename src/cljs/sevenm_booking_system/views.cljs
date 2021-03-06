(ns sevenm-booking-system.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]
            [pushy.core :as pushy]
            [ajax.core :as ajax]
            [cljs.reader :as reader]
            [sevenm-booking-system.routes :as routes]
            [sevenm-booking-system.config :refer [server session]]
            [sevenm-booking-system.components :as comp]
            [sevenm-booking-system.activitylog :as activity]))


;; Common Function for all components
;; ---------------------------------------------------
(defn set-div-to-disabled [star-rating-id]
  (do (.attr (js/$ ".box-primary :input") "disabled" true)
      (.rating (js/$ (str "#" star-rating-id)) "refresh"
               (clj->js {:disabled true
                         :showClear false
                         :showCaption true}))))

(defn tourgide-set-div-disabled [star-rating-id]
  (do
    (js/console.log "Hi")
    (.trigger (.prop (js/$ "#PerHours") "disabled" true)
              "chosen:updated")
    (.attr (js/$ ".box-primary :input") "disabled" true)
    (.rating (js/$ (str "#" star-rating-id)) "refresh"
             (clj->js {:disabled true
                       :showClear false
                       :showCaption true}))))

;; Unauthorized comp
;; -----------------------------
(defn unauthorized-panel []
  (fn []
    [:div.content-wrapper
     [:section.content-header
      [:h1 "\n        404 Error Page\n      "]]
     [:section.content
      [:div.error-page
       [:h2.headline.text-yellow " 404"]
       [:div.error-content
        [:h3 [:i.fa.fa-warning.text-yellow] " Oops! Page not found."]
        [:p "\n We could not find the page you were looking for.\n
          Meanwhile, you may "
         [:a {:href (routes/url-for :home)} "return to home"]]]]]]))

;; log-in page
;; -----------------------------
(defn login-page []
  [:div [comp/login-comp]])

;; register page
;; -----------------------------
(defn register-page []
  [:div [comp/register-comp]])

;; fit Quotations page
;; -----------------------------
(defn fit-quotations-page []
  [:div [comp/itineraries-tabs-comp 0
         [comp/fit-quotations-comp]]])

;; Group Quotations page
;; -----------------------------
(defn group-quotations-page []
  [:div [comp/itineraries-tabs-comp 0
         [comp/group-quotations-comp]]])

;; -----------------------------
;; fitbookings pages
;; -----------------------------
(defn fitbookings-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/fitbookings-table-comp]]])

(defn add-fitbooking-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-fitbooking comp/fitbookings-atom]]])

(defn edit-data-error [res busy]
  (case (:status res)
    401 (do (reset! session {})
            (pushy/set-token!
             routes/history (routes/url-for :home)))
    (reset! busy "error")))

(defn get-edit-data-fitbooking [id busy]
  (do
    (reset! busy "loading")
    (ajax/GET
     (str server "bookings/" id)
     {:handler #(do
                  (reset! busy "success")
                  (reset!
                   comp/fitbookings-atom
                   (comp/bookings-get-data-destruct-for-edit %)))
      :error-handler #(edit-data-error % busy)
      :format :json
      :response-format :json
      :keywords? true
      :headers
      {:Authorization
       (str "Token " (get-in @session [:session :token]))}})))

(defn edit-fitbooking-page [id]
  (let [busy (r/atom "loading")]
    (get-edit-data-fitbooking id busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp
                    id [comp/add-or-edit-fitbooking
                        comp/fitbookings-atom]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server, Please try after some time."]]])])))

;; -----------------------------
;; groupbookings pages
;; -----------------------------
(defn groupbookings-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/groupbookings-table-comp]]])

(defn add-groupbooking-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-groupbooking comp/groupbookings-atom]]])

(defn get-edit-data-groupbooking [id busy]
  (ajax/GET
   (str server "bookings/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! comp/groupbookings-atom
                        (comp/bookings-get-data-destruct-for-edit %)))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-groupbooking-page [id]
  (let [busy (r/atom "loading")]
    (get-edit-data-groupbooking id busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-groupbooking
                     comp/groupbookings-atom]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server, Please try after some time."]]])])))

;; ------------------------------------
;; items page or activities page
;; -----------------------------
(defn items-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/items-comp]]])

(defn add-item-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-item-comp
     {:adultcost 0
      :childcost 0
      :description ""
      :type "Both"
      :activitytitle ""
      :subcategory ""}]]])

(defn get-edit-data-items [id data busy]
  (ajax/GET
   (str server "items/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-item-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-items id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-item-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server, Please try after some time."]]])])))

(defn view-item [id data]
  (r/create-class
   {:component-did-mount
    #(set-div-to-disabled "HotelRating")
    :reagent-render
    (fn []
      [comp/itineraries-tabs-comp id
       [comp/add-or-edit-item-comp @data]])}))

(defn view-item-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-items id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [view-item id data]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

;; ---------------------------------------------------
;; vendors
;; ---------------------------------------------------
;; Hotels page
;; -----------------------------
(defn hotels-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/hotels-comp]]])

(defn add-hotel-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-hotel-comp
     {:rating 0
      :type "Not Contract"
      :cost 0
      :category "0 Star"
      :remarks ""
      :cancellationpolicy ""
      :address ""
      :phonenumber ""
      :faxnumber ""
      :contactpersonname ""
      :contactpersonphonenumber ""
      :contactpersonemail ""
      :description ""
      :filename ""}]]])

(defn get-edit-data-hotels [id data busy]
  (ajax/GET
   (str server "hotels/" id)
   {:handler #(do (reset! busy "success") (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-hotel-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-hotels id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-hotel-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

(defn view-hotels [id data]
  (r/create-class
   {:component-did-mount
    #(set-div-to-disabled "HotelRating")
    :reagent-render
    (fn []
      [comp/itineraries-tabs-comp id
       [comp/add-or-edit-hotel-comp @data]])}))

(defn view-hotel-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-hotels id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [view-hotels id data]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

;; restaurants page
;; -----------------------------
(defn restaurants-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/restaurants-comp]]])

(defn add-restaurant-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-restaurant-comp
     {:area ""
      :rating 0
      :cuisine ""
      :address ""
      :contactperson ""
      :contactnumber ""
      :faxnumber ""
      :cellnumber ""
      :lunch 0
      :dinner 0
      :kids 0
      :waytoconfirm ""
      :description ""
      :remarks ""
      :email ""}]]])

(defn get-edit-data-restaurants [id data busy]
  (ajax/GET
   (str server "restaurants/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-restaurant-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-restaurants id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-restaurant-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))


(defn view-restaurant [id data]
  (r/create-class
   {:component-did-mount
    #(set-div-to-disabled "RestaurantRating")
    :reagent-render
    (fn []
      [comp/itineraries-tabs-comp id
       [comp/add-or-edit-restaurant-comp @data]])}))


(defn view-restaurant-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-restaurants id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [view-restaurant id data]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))
;; tourguides page
;; -----------------------------
(defn tourguides-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/tourguides-comp]]])

(defn add-tourguide-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-tourguide-comp
     {:vendorid 0
      :rating 0
      :address ""
      :contactnumber ""
      :cellnumber ""
      :amount 0
      :hours 0
      :description ""
      :email ""}]]])

(defn get-edit-data-tourguides [id data busy]
  (ajax/GET
   (str server "tourguides/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-tourguide-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-tourguides id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-tourguide-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

(defn view-tourguide [id data]
  (r/create-class
   {:component-did-mount
    #(tourgide-set-div-disabled "TourGuideRating")
    :reagent-render
    (fn []
      [comp/itineraries-tabs-comp id
       [comp/add-or-edit-tourguide-comp @data]])}))


(defn view-tourguide-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-tourguides id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [view-tourguide id data]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

;; transportations page
;; -----------------------------
(defn transportations-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/transportations-comp]]])

(defn add-transportation-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-transportation-comp
     {:vendorid 0
      :rating 0
      :type "None"
      :address ""
      :contactperson ""
      :contactnumber ""
      :faxnumber ""
      :email ""
      :bankinformation ""
      :description ""}]]])

(defn get-edit-data-transportations [id data busy]
  (ajax/GET
   (str server "transportations/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-transportation-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-transportations id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-transportation-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

(defn view-transportation [id data]
  (r/create-class
   {:component-did-mount
    #(set-div-to-disabled "TransportRating")
    :reagent-render
    (fn []
      [comp/itineraries-tabs-comp id
       [comp/add-or-edit-transportation-comp @data]])}))

(defn view-transportation-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-transportations id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [view-transportation id data]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

;; events page
;; -----------------------------
(defn events-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/events-comp]]])

(defn add-event-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-event-comp
     {:vendorid 0 :adultcost 0 :childcost 0
      :rating 0 :st 0 :cp 0 :contactnumber ""
      :phonenumber "" :faxnumber "" :website ""
      :email "" :actual "" :meal "" :remarks ""
      :contract "" :description "" :bookingguide ""}]]])

(defn get-edit-data-events [id data busy]
  (ajax/GET
   (str server "events/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-event-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-events id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-event-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

(defn view-event [id data]
  (r/create-class
   {:component-did-mount
    #(set-div-to-disabled "EventRating")
    :reagent-render
    (fn []
      [comp/itineraries-tabs-comp id
       [comp/add-or-edit-event-comp @data]])}))

(defn view-event-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-events id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [view-event id data]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server, Please try after some time."]]])])))

;; Cities page
;; -----------------------------
(defn cities-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/cities-comp]]])

(defn add-city-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-city-comp {}]]])

(defn get-edit-data-cities [id data busy]
  (ajax/GET
   (str server "cities/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-city-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-cities id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-city-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server,
                Please try after some time."]]])])))

;; logos page
;; -----------------------------
(defn logos-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/logos-comp]]])

(defn add-logo-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-logo-comp
     {:logoname "" :filename ""}]]])

(defn get-edit-data-logos [id data busy]
  (ajax/GET
   (str server "logos/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-logo-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-logos id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-logo-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server, Please try after some time."]]])])))


;; users page
;; -----------------------------
(defn all-users-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/users-comp]]])

(defn add-user-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [comp/add-or-edit-user-comp
     {:role "bookadmin" :mobilenumber ""}]]])

(defn get-edit-data-users [id data busy]
  (ajax/GET
   (str server "users/" id)
   {:handler #(do
                (reset! busy "success")
                (reset! data %))
    :error-handler #(edit-data-error % busy)
    :format :json
    :response-format :json
    :keywords? true
    :headers
    {:Authorization
     (str "Token " (get-in @session [:session :token]))}}))

(defn edit-user-page [id]
  (let [data (r/atom {})
        busy (r/atom "loading")]
    (get-edit-data-users id data busy)
    (fn []
      [:div
       (case @busy
         "loading" [comp/itineraries-tabs-comp id
                    [:div>center
                     [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
                     " Loading..."]]
         "success" [comp/itineraries-tabs-comp id
                    [comp/add-or-edit-user-comp @data]]
         [comp/itineraries-tabs-comp id
          [:div.callout.callout-warning
           [:h4 [:i.icon.fa.fa-warning] " Warning!"]
           [:p "Problem With the server, Please try after some time."]]])])))



;; Activity log components
;; -----------------------------
(defn complete-activity-log-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [activity/activity-log-comp
     "Complete Activity Log" "activitylog?"]]])

(defn fit-activitylog-page [id]
  [:div
   [comp/itineraries-tabs-comp id
    [activity/activity-log-comp
     "Fit Booking Activity Log"
     (str "activitylog?bookingid=" id "&")]]])

(defn group-activitylog-page [id]
  [:div
   [comp/itineraries-tabs-comp id
    [activity/activity-log-comp
     "Group Booking Activity Log"
     (str "activitylog?bookingid=" id "&")]]])

;; Payment log components
;; -----------------------------
(defn complete-payment-log-page []
  [:div
   [comp/itineraries-tabs-comp 0
    [activity/paymentlog-comp
     "Complete Payment Log" "paymentlog?"]]])

(defn fit-paymentlog-page [id]
  [:div
   [comp/itineraries-tabs-comp id
    [activity/paymentlog-comp
     "Fit Booking Payment Log"
     (str "paymentlog?bookingid=" id "&")]]])

(defn group-paymentlog-page [id]
  [:div
   [comp/itineraries-tabs-comp id
    [activity/activity-log-comp
     "Group Booking Payment Log"
     (str "paymentlog?bookingid=" id "&")]]])

;; -------------------------------------------------
;; Not Used Pages
;; -------------------------------------------------
;; vendors page
;; -----------------------------
;; (defn vendors-page []
;;   [:div
;;    [comp/itineraries-tabs-comp 0
;;     [comp/vendors-comp]]])

;; (defn add-vendor-page []
;;   [:div
;;    [comp/itineraries-tabs-comp 0
;;     [comp/add-or-edit-vendor-comp {}]]])

;; (defn get-edit-data-vendors [id data busy]
;;   (ajax/GET
;;    (str server "vendors/" id)
;;    {:handler #(do
;;                 (reset! busy "success")
;;                 (reset! data %))
;;     :error-handler #(edit-data-error % busy)
;;     :format :json
;;     :response-format :json
;;     :keywords? true
;;     :headers
;;     {:Authorization
;;      (str "Token "
;;           (get-in @session [:session :token]))}}))

;; (defn edit-vendor-page [id]
;;   (let [data (r/atom {})
;;         busy (r/atom "loading")]
;;     (get-edit-data-vendors id data busy)
;;     (fn []
;;       [:div
;;        (case @busy
;;          "loading" [comp/itineraries-tabs-comp id
;;                     [:div>center
;;                      [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
;;                      " Loading..."]]
;;          "success"   [comp/itineraries-tabs-comp id
;;                       [comp/add-or-edit-vendor-comp @data]]
;;          [comp/itineraries-tabs-comp id
;;           [:div.callout.callout-warning
;;            [:h4 [:i.icon.fa.fa-warning] " Warning!"]
;;            [:p "Problem With the server,
;;                 Please try after some time."]]])])))

;; ;; bookingfits page
;; ;; -----------------------------
;; (defn bookingfits-page []
;;   [:div
;;    [comp/itineraries-tabs-comp 0
;;     [comp/bookingfits-comp]]])

;; (defn add-bookingfit-page []
;;   [:div
;;    [comp/itineraries-tabs-comp 0
;;     [comp/add-or-edit-bookingfit-comp {}]]])

;; (defn get-edit-data-bookingfits [id data busy]
;;   (ajax/GET
;;    (str server "bookingfits/" id)
;;    {:handler #(do
;;                 (reset! busy "success")
;;                 (reset! data %))
;;     :error-handler #(edit-data-error % busy)
;;     :format :json
;;     :response-format :json
;;     :keywords? true
;;     :headers
;;     {:Authorization
;;      (str "Token " (get-in @session [:session :token]))}}))

;; (defn edit-bookingfit-page [id]
;;   (let [data (r/atom {})
;;         busy (r/atom "loading")]
;;     (get-edit-data-bookingfits id data busy)
;;     (fn []
;;       [:div
;;        (case @busy
;;          "loading" [comp/itineraries-tabs-comp id
;;                     [:div>center
;;                      [:i.fa.fa-spinner.fa-pulse.fa-5x.fa-fw]
;;                      " Loading..."]]
;;          "success" [comp/itineraries-tabs-comp id
;;                     [comp/add-or-edit-bookingfit-comp @data]]
;;          [comp/itineraries-tabs-comp id
;;           [:div.callout.callout-warning
;;            [:h4 [:i.icon.fa.fa-warning] " Warning!"]
;;            [:p "Problem With the server, Please try after some time."]]])])))

;; end of not used pages
;; -------------------------------------------------


;; -------------------------------
;; panels
;; -------------------------------

;; login or home panel
;; -------------------------
(defn home-panel []
  [:div
   (let [role (get-in @session [:session :user :role])]
     (case role
       "bookadmin" (pushy/set-token!
                    routes/history
                    (routes/url-for :fit-quotations))
       "bookstandard" (pushy/set-token!
                       routes/history
                       (routes/url-for :fit-quotations))
       "bookgroup" (pushy/set-token!
                    routes/history
                    (routes/url-for :fit-quotations))
       [login-page]))])

;; FIT quotations panel
;; -------------------------
(defn fit-quotations-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [fit-quotations-page]
      "bookstandard" [fit-quotations-page]
      "bookgroup"  [unauthorized-panel]
      [login-page])))

;; group quotations panel
;; -------------------------
(defn group-quotations-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [group-quotations-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [group-quotations-page]
      [login-page])))

;; register panel
;; -------------------------
(defn register-panel []
  [register-page])

;; fitbookings
;; -------------------------
(defn fitbookings-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [fitbookings-page]
      "bookstandard" [fitbookings-page]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn add-fitbooking-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-fitbooking-page]
      "bookstandard" [add-fitbooking-page]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-fitbooking-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-fitbooking-page (get-in route-details [:route-params :id])]
      "bookstandard" [edit-fitbooking-page (get-in route-details [:route-params :id])]
      "bookgroup" [unauthorized-panel]
      [login-page])))

;; groupbookings
;; -------------------------
(defn groupbookings-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [groupbookings-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [groupbookings-page]
      [login-page])))

(defn add-groupbooking-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-groupbooking-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [add-groupbooking-page]
      [login-page])))

(defn edit-groupbooking-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-groupbooking-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [edit-groupbooking-page (get-in route-details [:route-params :id])]
      [login-page])))

;; items or activities
;; -------------------------
(defn items-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [items-page]
      "bookstandard" [items-page]
      "bookgroup" [items-page]
      [login-page])))

(defn add-item-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-item-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-item-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-item-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn view-item-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [view-item-page (get-in route-details [:route-params :id])]
      "bookstandard" [view-item-page (get-in route-details [:route-params :id])]
      "bookgroup" [view-item-page (get-in route-details [:route-params :id])]
      [login-page])))

;; Cities
;; -------------------------
(defn cities-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [cities-page]
      "bookstandard" [cities-page]
      "bookgroup" [cities-page]
      [login-page])))

(defn add-city-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-city-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-city-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-city-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))


;; Hotels
;; -------------------------
(defn hotels-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [hotels-page]
      "bookstandard" [hotels-page]
      "bookgroup" [hotels-page]
      [login-page])))

(defn add-hotel-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-hotel-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-hotel-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-hotel-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn view-hotel-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [view-hotel-page (get-in route-details [:route-params :id])]
      "bookstandard" [view-hotel-page (get-in route-details [:route-params :id])]
      "bookgroup" [view-hotel-page (get-in route-details [:route-params :id])]
      [login-page])))

;; Restaurants
;; -------------------------
(defn restaurants-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [restaurants-page]
      "bookstandard" [restaurants-page]
      "bookgroup" [restaurants-page]
      [login-page])))

(defn add-restaurant-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-restaurant-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-restaurant-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-restaurant-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn view-restaurant-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [view-restaurant-page (get-in route-details [:route-params :id])]
      "bookstandard" [view-restaurant-page (get-in route-details [:route-params :id])]
      "bookgroup" [view-restaurant-page (get-in route-details [:route-params :id])]
      [login-page])))


;; Tourguides
;; -------------------------
(defn tourguides-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [tourguides-page]
      "bookstandard" [tourguides-page]
      "bookgroup" [tourguides-page]
      [login-page])))

(defn add-tourguide-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-tourguide-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-tourguide-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-tourguide-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn view-tourguide-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [view-tourguide-page (get-in route-details [:route-params :id])]
      "bookstandard" [view-tourguide-page (get-in route-details [:route-params :id])]
      "bookgroup" [view-tourguide-page (get-in route-details [:route-params :id])]
      [login-page])))

;; Transportations
;; -------------------------
(defn transportations-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [transportations-page]
      "bookstandard" [transportations-page]
      "bookgroup" [transportations-page]
      [login-page])))

(defn add-transportation-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-transportation-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-transportation-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-transportation-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn view-transportation-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [view-transportation-page (get-in route-details [:route-params :id])]
      "bookstandard" [view-transportation-page (get-in route-details [:route-params :id])]
      "bookgroup" [view-transportation-page (get-in route-details [:route-params :id])]
      [login-page])))

;; Events
;; -------------------------
(defn events-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [events-page]
      "bookstandard" [events-page]
      "bookgroup" [events-page]
      [login-page])))

(defn add-event-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-event-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-event-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-event-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn view-event-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [view-event-page (get-in route-details [:route-params :id])]
      "bookstandard" [view-event-page (get-in route-details [:route-params :id])]
      "bookgroup" [view-event-page (get-in route-details [:route-params :id])]
      [login-page])))

;; Logos
;; -------------------------
(defn logos-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [logos-page]
      "bookstandard" [logos-page]
      "bookgroup" [logos-page]
      [login-page])))

(defn add-logo-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-logo-page]
      "bookstandard" [add-logo-page]
      "bookgroup" [add-logo-page]
      [login-page])))

(defn edit-logo-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-logo-page (get-in route-details [:route-params :id])]
      "bookstandard" [edit-logo-page (get-in route-details [:route-params :id])]
      "bookgroup" [edit-logo-page (get-in route-details [:route-params :id])]
      [login-page])))


;; activity-logs
;; -------------------------
(defn complete-activity-log-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [complete-activity-log-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn fit-activitylog-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [fit-activitylog-page (get-in route-details [:route-params :id])]
      "bookstandard" [fit-activitylog-page (get-in route-details [:route-params :id])]
      "bookgroup"  [unauthorized-panel]
      [login-page])))

(defn group-activitylog-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [group-activitylog-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [group-activitylog-page (get-in route-details [:route-params :id])]
      [login-page])))

;; payment logs
;; -------------------------------------
(defn complete-payment-log-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [complete-payment-log-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn fit-paymentlog-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [fit-paymentlog-page (get-in route-details [:route-params :id])]
      "bookstandard" [fit-paymentlog-page (get-in route-details [:route-params :id])]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn group-paymentlog-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [group-paymentlog-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [group-paymentlog-page (get-in route-details [:route-params :id])]
      [login-page])))

;; Users
;; -------------------------
(defn all-users-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [all-users-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn add-user-panel []
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [add-user-page]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

(defn edit-user-panel [route-details]
  (let [role (get-in @session [:session :user :role])]
    (case role
      "bookadmin" [edit-user-page (get-in route-details [:route-params :id])]
      "bookstandard" [unauthorized-panel]
      "bookgroup" [unauthorized-panel]
      [login-page])))

;; -----------------------------------
;; not used panels
;; -----------------------------------
;; vendors
;; -------------------------
;; (defn vendors-panel []
;;   (if (seq (@session :session))
;;     [vendors-page]
;;     [login-page]))

;; (defn add-vendor-panel []
;;   (if (seq (@session :session))
;;     [add-vendor-page]
;;     [login-page]))

;; (defn edit-vendor-panel [route-details]
;;   (if (seq (@session :session))
;;     [edit-vendor-page (get-in route-details [:route-params :id])]
;;     [login-page]))

;; ;; Bookingfits
;; ;; -------------------------
;; (defn bookingfits-panel []
;;   (if (seq (@session :session))
;;     [bookingfits-page]
;;     [login-page]))

;; (defn add-bookingfit-panel []
;;   (if (seq (@session :session))
;;     [add-bookingfit-page]
;;     [login-page]))

;; (defn edit-bookingfit-panel [route-details]
;;   (if (seq (@session :session))
;;     [edit-bookingfit-page (get-in route-details [:route-params :id])]
;;     [login-page]))


;; --------------------
(defmulti panels (fn [route-details]
                   (:handler route-details)))
(defmethod panels :home [r] [home-panel])
(defmethod panels :register [r] [register-panel])
(defmethod panels :fit-quotations [r] [fit-quotations-panel])
(defmethod panels :group-quotations [r] [group-quotations-panel])
(defmethod panels :fitbookings [r] [fitbookings-panel])
(defmethod panels :add-fitbooking [r] [add-fitbooking-panel])
(defmethod panels :edit-fitbooking [r] [edit-fitbooking-panel r])
(defmethod panels :fit-activitylog [r] [fit-activitylog-panel r])
(defmethod panels :fit-paymentlog [r] [fit-paymentlog-panel r])
(defmethod panels :groupbookings [r] [groupbookings-panel])
(defmethod panels :add-groupbooking [r] [add-groupbooking-panel])
(defmethod panels :edit-groupbooking [r] [edit-groupbooking-panel r])
(defmethod panels :group-activitylog [r] [group-activitylog-panel r])
(defmethod panels :group-paymentlog [r] [group-paymentlog-panel r])

(defmethod panels :items [r] [items-panel])
(defmethod panels :add-item [r] [add-item-panel])
(defmethod panels :edit-item [r] [edit-item-panel r])
(defmethod panels :view-item [r] [view-item-panel r])

(defmethod panels :cities [r] [cities-panel])
(defmethod panels :add-city [r] [add-city-panel])
(defmethod panels :edit-city [r] [edit-city-panel r])

(defmethod panels :hotels [r] [hotels-panel])
(defmethod panels :add-hotel [r] [add-hotel-panel])
(defmethod panels :edit-hotel [r] [edit-hotel-panel r])
(defmethod panels :view-hotel [r] [view-hotel-panel r])

(defmethod panels :restaurants [r] [restaurants-panel])
(defmethod panels :add-restaurant [r] [add-restaurant-panel])
(defmethod panels :edit-restaurant [r] [edit-restaurant-panel r])
(defmethod panels :view-restaurant [r] [view-restaurant-panel r])

(defmethod panels :tourguides [r] [tourguides-panel])
(defmethod panels :add-tourguide [r] [add-tourguide-panel])
(defmethod panels :edit-tourguide [r] [edit-tourguide-panel r])
(defmethod panels :view-tourguide [r] [view-tourguide-panel r])

(defmethod panels :transportations [r] [transportations-panel])
(defmethod panels :add-transportation [r] [add-transportation-panel])
(defmethod panels :edit-transportation [r] [edit-transportation-panel r])
(defmethod panels :view-transportation [r] [view-transportation-panel r])

(defmethod panels :events [r] [events-panel])
(defmethod panels :add-event [r] [add-event-panel])
(defmethod panels :edit-event [r] [edit-event-panel r])
(defmethod panels :view-event [r] [view-event-panel r])

(defmethod panels :logos [r] [logos-panel])
(defmethod panels :add-logo [r] [add-logo-panel])
(defmethod panels :edit-logo [r] [edit-logo-panel r])

(defmethod panels :all-users [r] [all-users-panel])
(defmethod panels :add-user [r] [add-user-panel])
(defmethod panels :edit-user [r] [edit-user-panel r])

(defmethod panels :complete-activity-log [r] [complete-activity-log-panel])
(defmethod panels :complete-payment-log [r] [complete-payment-log-panel])

;; (defmethod panels :bookingfits [r] [bookingfits-panel])
;; (defmethod panels :add-bookingfit [r] [add-bookingfit-panel])
;; (defmethod panels :edit-bookingfit [r] [edit-bookingfit-panel r])
;; (defmethod panels :vendors [r] [vendors-panel])
;; (defmethod panels :add-vendor [r] [add-vendor-panel])
;; (defmethod panels :edit-vendor [r] [edit-vendor-panel r])

(defmethod panels :unauthorized [r] [unauthorized-panel])
(defmethod panels :default [r] [:div])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      (panels @active-panel))))
