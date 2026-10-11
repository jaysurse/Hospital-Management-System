package com.hospital.model;

/**
 * Itemized charge within a medical invoice.
 * Demonstrates Composition: BillItem cannot exist independently of a Bill.
 */
public class BillItem implements Identifiable<Integer> {

    private int itemId;
    private int billId;
    private String description;
    private double amount;

    public BillItem() {
    }

    public BillItem(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public BillItem(int itemId, int billId, String description, double amount) {
        this.itemId = itemId;
        this.billId = billId;
        this.description = description;
        this.amount = amount;
    }

    @Override
    public Integer getId() {
        return itemId;
    }

    @Override
    public void setId(Integer id) {
        this.itemId = (id == null) ? 0 : id;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("%s: ₹%.2f", description, amount);
    }
}
