package com.example.myapplication.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class Expense {

    @SerializedName("id")
    private String id;

    @SerializedName("amount")
    private double amount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("category")
    private String category;

    @SerializedName("remark")
    private String remark;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("receiptImageUrl")
    private String receiptImageUrl; // NEW FIELD for local image path

    @JsonAdapter(ISO8601DateAdapter.class)
    private Date createdDate;

    // Default constructor (required for Firebase or Gson)
    public Expense(String id, double amount, String currency, String category, String remark, String createdBy, Date createdDate) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.remark = remark;
        this.createdBy = createdBy;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.receiptImageUrl = receiptImageUrl;
    }

    // Overloaded constructor (without id and receipt)
    public Expense(double amount, String currency, Date createdDate, String category, String remark, String createdBy) {
        this.id = UUID.randomUUID().toString();
        this.amount = amount;
        this.currency = currency;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.category = category;
        this.remark = remark;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getReceiptImage() {
        return receiptImageUrl;
    }


    public void setReceiptImageUrl(String receiptImageUrl) {
        this.receiptImageUrl = receiptImageUrl;
    }

    // Date adapter for ISO 8601 formatting
    public static class ISO8601DateAdapter extends TypeAdapter<Date> {
        private final SimpleDateFormat formatter;

        public ISO8601DateAdapter() {
            formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public void write(JsonWriter out, Date date) throws IOException {
            if (date == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(date));
            }
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            try {
                return formatter.parse(in.nextString());
            } catch (Exception e) {
                return null;
            }
        }
    }
}
