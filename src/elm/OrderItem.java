package elm;

public class OrderItem {
	private String discountCode;
	private String shippingMethod;
	private int lineItemQuantity;
	private String lineItemName;
	private String lineItemSKU;
	private String shippingName;
	private String shippingAddress1;
	private String shippingCity;
	private String notes;
	
	public OrderItem(String discountCode, String shippingMethod, int lineItemQuantity, String lineItemName, String lineItemSKU, String shippingName, String shippingAddress1, String shippingCity, String notes){
		
		this.discountCode=discountCode;
		this.shippingMethod=shippingMethod;
		this.lineItemQuantity=lineItemQuantity;
		this.lineItemName=lineItemName;
		this.lineItemSKU=lineItemSKU;
		this.shippingName=shippingName;
		this.shippingAddress1=shippingAddress1;
		this.shippingCity=shippingCity;
		this.notes=notes;
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

}
