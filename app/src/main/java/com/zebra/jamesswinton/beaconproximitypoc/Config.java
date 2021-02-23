package com.zebra.jamesswinton.beaconproximitypoc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public class Config {

    @SerializedName("scan_parameters")
    @Expose
    private ScanParameters scanParameters;
    @SerializedName("beacons")
    @Expose
    private List<Beacon> beacons = null;

    public ScanParameters getScanParameters() {
        return scanParameters;
    }

    public void setScanParameters(ScanParameters scanParameters) {
        this.scanParameters = scanParameters;
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public void setBeacons(List<Beacon> beacons) {
        this.beacons = beacons;
    }

    public class ScanParameters {

        @SerializedName("scan_duration")
        @Expose
        private Integer scanDuration;
        @SerializedName("interval_between_scans")
        @Expose
        private Integer intervalBetweenScans;
        @SerializedName("perform_beacon_ranging")
        @Expose
        private Boolean performBeaconRanging;
        @SerializedName("meters_to_trigger_promotion")
        @Expose
        private Double metersToTriggerPromotion;
        @SerializedName("perform_beacon_rssi_ranging")
        @Expose
        private Boolean performBeaconRssiRanging;
        @SerializedName("threshold_rssi_to_trigger_promotions")
        @Expose
        private Integer thresholdRssiToTriggerPromotions;
        @SerializedName("limit_promotions_to_once_per_trip")
        @Expose
        private Boolean limitPromotions;
        @SerializedName("limit_promotions_timeout")
        @Expose
        private Integer limitPromotionsTimeout;
        @SerializedName("beacon_layouts")
        @Expose
        private List<String> beaconLayouts = null;

        public Integer getScanDuration() {
            return scanDuration;
        }

        public void setScanDuration(Integer scanDuration) {
            this.scanDuration = scanDuration;
        }

        public Integer getIntervalBetweenScans() {
            return intervalBetweenScans;
        }

        public void setIntervalBetweenScans(Integer intervalBetweenScans) {
            this.intervalBetweenScans = intervalBetweenScans;
        }

        public Boolean getPerformBeaconRanging() {
            return performBeaconRanging;
        }

        public void setPerformBeaconRanging(Boolean performBeaconRanging) {
            this.performBeaconRanging = performBeaconRanging;
        }

        public Double getMetersToTriggerPromotion() {
            return metersToTriggerPromotion;
        }

        public void setMetersToTriggerPromotion(Double metersToTriggerPromotion) {
            this.metersToTriggerPromotion = metersToTriggerPromotion;
        }

        public Boolean getPerformBeaconRssiRanging() {
            return performBeaconRssiRanging;
        }

        public void setPerformBeaconRssiRanging(Boolean performBeaconRssiRanging) {
            this.performBeaconRssiRanging = performBeaconRssiRanging;
        }

        public Integer getThresholdRssiToTriggerPromotions() {
            return thresholdRssiToTriggerPromotions;
        }
        public void setThresholdRssiToTriggerPromotions(Integer thresholdRssiToTriggerPromotions) {
            this.thresholdRssiToTriggerPromotions = thresholdRssiToTriggerPromotions;
        }

        public Boolean getLimitPromotions() {
            return limitPromotions;
        }

        public void setLimitPromotions(Boolean limitPromotions) {
            this.limitPromotions = limitPromotions;
        }

        public Integer getLimitPromotionsTimeout() {
            return limitPromotionsTimeout;
        }

        public void setLimitPromotionsTimeout(Integer limitPromotionsTimeout) {
            this.limitPromotionsTimeout = limitPromotionsTimeout;
        }

        public List<String> getBeaconLayouts() {
            return beaconLayouts;
        }

        public void setBeaconLayouts(List<String> beaconLayouts) {
            this.beaconLayouts = beaconLayouts;
        }
    }

    public class Beacon {

        @SerializedName("beacon_name")
        @Expose
        private String beaconName;
        @SerializedName("beacon_mac")
        @Expose
        private String beaconMac;
        @SerializedName("promotion_title")
        @Expose
        private String promotionTitle;
        @SerializedName("promotion_image")
        @Expose
        private String promotionImage;
        @SerializedName("promotion_message")
        @Expose
        private String promotionMessage;
        @SerializedName("promotion_splash")
        @Expose
        private String promotionSplash;
        @SerializedName("promotion_timeout")
        @Expose
        private Integer promotionTimeout;
        @SerializedName("TTS_enabled")
        @Expose
        private Boolean tTSEnabled;
        @SerializedName("TTS")
        @Expose
        private String tTS;

        private Long showPromotionTimestamp;

        public Long getShowPromotionTimestamp() {
            return showPromotionTimestamp;
        }

        public void setShowPromotionTimestamp() {
            Date date = new Date();
            this.showPromotionTimestamp = date.getTime();
        }

        public String getBeaconName() {
            return beaconName;
        }

        public void setBeaconName(String beaconMac) {
            this.beaconName = beaconName;
        }

        public String getBeaconMac() {
            return beaconMac;
        }

        public void setBeaconMac(String beaconMac) {
            this.beaconMac = beaconMac;
        }

        public String getPromotionTitle() {
            return promotionTitle;
        }

        public void setPromotionTitle(String promotionTitle) {
            this.promotionTitle = promotionTitle;
        }

        public String getPromotionImage() {
            return promotionImage;
        }

        public void setPromotionImage(String promotionImage) {
            this.promotionImage = promotionImage;
        }

        public String getPromotionMessage() {
            return promotionMessage;
        }

        public void setPromotionMessage(String promotionMessage) {
            this.promotionMessage = promotionMessage;
        }

        public String getPromotionSplash() {
            return promotionSplash;
        }

        public void setPromotionSplash(String promotionSplash) {
            this.promotionSplash = promotionSplash;
        }

        public Integer getPromotionTimeout() {
            return promotionTimeout;
        }

        public void setPromotionTimeout(Integer promotionTimeout) {
            this.promotionTimeout = promotionTimeout;
        }

        public Boolean getTTSEnabled() {
            return tTSEnabled;
        }

        public void setTTSEnabled(Boolean tTSEnabled) {
            this.tTSEnabled = tTSEnabled;
        }

        public String getTTS() {
            return tTS;
        }

        public void setTTS(String tTS) {
            this.tTS = tTS;
        }

    }

}
