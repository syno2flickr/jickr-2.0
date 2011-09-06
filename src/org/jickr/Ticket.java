package org.jickr;

enum TicketCompleteStatus{
	NOT_COMPLETED(0),
	COMPLETED(1),
	COMPLETED_WITH_ERROR(2);
	
	private int value;
	
	private TicketCompleteStatus(int value) {
		this.value = value;
	}
	
	public static TicketCompleteStatus valueOf(int value) throws FlickrException{
		switch(value){
		case 0 : return TicketCompleteStatus.NOT_COMPLETED;
		case 1 : return TicketCompleteStatus.COMPLETED;
		case 2 : return TicketCompleteStatus.COMPLETED_WITH_ERROR;
		default : throw new FlickrException("TicketCompleteStatus value must be between 0 and 2");
		}
	}
}

public class Ticket {
	
	private String ticketid;
	private TicketCompleteStatus completedStatus;
	private Boolean invalid;
	private String photoid;
	private String imported;
	
	public Ticket(String ticketid,TicketCompleteStatus completedStatus,
					Boolean invalid,String photoid,String imported) {
		this.ticketid = ticketid;
		this.completedStatus = completedStatus;
		this.invalid = invalid;
		this.photoid = photoid;
		this.imported = imported;
	}
	
	public String getTicketid() {
		return ticketid;
	}
	public void setTicketid(String ticketid) {
		this.ticketid = ticketid;
	}
	public TicketCompleteStatus getCompletedStatus() {
		return completedStatus;
	}
	public void setCompletedStatus(TicketCompleteStatus completedStatus) {
		this.completedStatus = completedStatus;
	}
	public Boolean getInvalid() {
		return invalid;
	}
	public void setInvalid(Boolean invalid) {
		this.invalid = invalid;
	}
	public String getPhotoid() {
		return photoid;
	}
	public void setPhotoid(String photoid) {
		this.photoid = photoid;
	}
	public String getImported() {
		return imported;
	}
	public void setImported(String imported) {
		this.imported = imported;
	}
}
