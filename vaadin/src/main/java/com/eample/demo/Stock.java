package com.eample.demo;

import java.io.Serializable;



/**
 * A entity object, like in any other Java application. In a typical real world
 * application this could for example be a JPA entity.
 */
@SuppressWarnings("serial")

public class Stock implements Serializable, Cloneable {

	private Long id;

	private String exchange = "";

	private String symbol = "";
private String name;
	private double bid;
	private double ask;
	private double volume;
	private double open;
	private double shares;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Get the value of symbol
	 *
	 * @return the value of symbol
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Set the value of symbol
	 *
	 * @param symbol
	 *            new value of symbol
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * Get the value of exchange
	 *
	 * @return the value of exchange
	 */
	public String getExchange() {
		return exchange;
	}

	/**
	 * Set the value of exchange
	 *
	 * @param exchange
	 *            new value of exchange
	 */
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}


	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getAsk() {
		return ask;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getShares() {
		return shares;
	}

	public void setShares(double shares) {
		this.shares = shares;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (this.id == null) {
			return false;
		}

		if (obj instanceof Stock && obj.getClass().equals(getClass())) {
			return this.id.equals(((Stock) obj).id);
		}

		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 43 * hash + (id == null ? 0 : id.hashCode());
		return hash;
	}

	@Override
	public Stock clone() throws CloneNotSupportedException {
		return (Stock) super.clone();
	}

	@Override
	public String toString() {
		return exchange + " " + symbol;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName(){
		return this.name;
	}
}
