package org.rockhopper.smarhome.hsb.server;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

// {"datetime": "2024-02-12 14:59:35", "number": "+336...", "": "Whatsapp"}
public class SMS implements Serializable{
	private static final long serialVersionUID = -8576835180958704844L;
	
	LocalDateTime datetime;
	String number;
	String text;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public LocalDateTime getDatetime() {
		return datetime;
	}
	public void setDatetime(LocalDateTime datetime) {
		this.datetime = datetime;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
