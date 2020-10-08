package com.springboot.model;

import java.io.Serializable;
import java.util.Date;

public class CurrencyRate implements Serializable {

	private static final long serialVersionUID = 1L;

	private String currency;
	private String base;
	private double rate;
	private Date date;

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String toString() {
		return this.getCurrency() + ": " + this.getRate();
	}
}
