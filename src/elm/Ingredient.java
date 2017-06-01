package elm;

public class Ingredient {
	private String ingredientName;
	private Integer ingredientQuantity=0;
	

	public Ingredient(String ingredientName) {
		// TODO Auto-generated constructor stub
		this.ingredientName=ingredientName;
		//this.ingredientQuantity=0;
	}
	
	public void addQuantity(int quantity, int weight){
		ingredientQuantity+=quantity*weight;
	}
	
	public int getQuantity(){
		return ingredientQuantity;
	}
	
	public String getName(){
		return ingredientName;
	}

}
