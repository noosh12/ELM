package elm;

public class Ingredient {
	private String ingredientName;
	private Double ingredientQuantity=0.0;
	private Double multiplier = 1.0;
	private String unit;

	public Ingredient(String ingredientName, double multi, String unit) {
		this.ingredientName=ingredientName;
		this.multiplier=multi;
		this.unit=unit;
	}
	
	public Ingredient(String ingredientName) {
		this.ingredientName=ingredientName;
	}
	
	public void addQuantity(int quantity, double weight){
		this.ingredientQuantity+=quantity*weight;
	}
	
	public double getQuantity(){
		return this.ingredientQuantity;
	}
	
	public String getFinalQuantity(){
		if(this.unit.equalsIgnoreCase("kg"))
			return Double.toString(this.ingredientQuantity/1000)+","+ this.unit;
		else
			return this.ingredientQuantity+","+this.unit;
		
	}
	
	public String getName(){
		return this.ingredientName;
	}
	
	public Double getMultiplier(){
		return this.multiplier;
	}

}
