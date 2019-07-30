package elm;

public class OrderItem {
	private String orderID;
	private String discountCode;
	private String shippingMethod;
	private int lineItemQuantity;
	private String lineItemName;
	private String lineItemSKU;
	private String shippingName;
	private String shippingAddress1;
	private String shippingCity;
	private String notes;
	private String shippingPhone;
	private String email;
	private String shippingPostcode;
	private String vendor;
	
	public OrderItem(String orderID, String discountCode, String shippingMethod, int lineItemQuantity, String lineItemName, String lineItemSKU, String shippingName, String shippingAddress1, String shippingCity, String shippingPostcode, String notes, String shippingPhone, String email, String vendor){
		
		this.orderID=orderID;
		this.discountCode=discountCode;
		this.shippingMethod=shippingMethod;
		this.lineItemQuantity=lineItemQuantity;
		this.lineItemName=lineItemName;
		this.lineItemSKU=lineItemSKU;
		this.shippingName=shippingName;
		this.shippingAddress1=shippingAddress1;
		this.shippingCity=shippingCity;
		this.shippingPostcode=shippingPostcode;
		this.notes=notes;
		this.shippingPhone=shippingPhone;
		this.email=email;
		this.vendor=vendor;
	}
	
	public String getDiscountCode(){
		return discountCode;
	}
	
	public String getShippingMethod(){
		return shippingMethod;
	}
	
	public int getLineItemQuantity(){
		return lineItemQuantity;
	}
	
	public String getLineItemName(){
		return lineItemName;
	}
	
	public String getLineItemSKU(){
		return lineItemSKU;
	}
	
	public String getShippingName(){
		return shippingName;
	}
	
	public String getShippingAddress(){
		return shippingAddress1 + " " + shippingCity;
	}
	
	public String getNotes(){
		return notes;
	}
	
	public String getOrderID(){
		return orderID;
	}
	
	public void setNotes(String newNotes){
		notes=newNotes;
	}
	
	public String getShippingPhone(){
		return shippingPhone;
	}
	
	public String getEmail(){
		return email;
	}
	
	public String getVendor(){
		return vendor;
	}
	
	public String getFullShippingString(){
		return orderID + "," + shippingName + "," + shippingAddress1 + "," + shippingCity + "," + shippingPostcode + "," + shippingPhone  + "," + notes;
	}

}
