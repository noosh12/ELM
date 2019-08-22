package elm;

public class Ingredient {
	private String ingredientName;
	private Double ingredientQuantity=0.0;
	private Double multiplier = 1.0;
	

	public Ingredient(String ingredientName, double multi) {
		this.ingredientName=ingredientName;
		this.multiplier=multi;
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
		if(this.ingredientName.contains("BASA"))
			return this.ingredientQuantity+","+"fillets";
		else if(this.ingredientName.contains("OMELETTE"))
			return this.ingredientQuantity+","+"pieces";
		else if(this.ingredientName.contains("SAUCE"))
			return this.ingredientQuantity+","+"mL";
		else
			return Double.toString(this.ingredientQuantity/1000)+","+"Kg";
	}
	
	public String getName(){
		return this.ingredientName;
	}
	
	public Double getMultiplier(){
		return this.multiplier;
	}

}
