package elm;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

public class FileFunctions
{
	public static void main( String [] args )
	{
		int totalQuantity = 0;
		int count = 0;
		List<OrderItem> orderLine = new ArrayList<>();							//stores the OrderItem objects
		HashMap<String, Integer> skuQuantities= new HashMap<String,Integer>();	//meal quantities
		HashMap<String, String> skuNames= new HashMap<String,String>();			//meal names
		HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod = new HashMap<String, ArrayList<OrderItem>> (); // Orders by shipping method
		
		ArrayList<String> skuList = new ArrayList<String>(); //Arraylist that holds skus - for sorting
		ArrayList<String> namesList = new ArrayList<String>(); //Arraylist that holds names - for sorting
		ArrayList<String> namesListGMD = new ArrayList<String>(); //Arraylist that holds names - for sorting
		ArrayList<String> namesListSpecials = new ArrayList<String>(); //Arraylist that holds names - for sorting
		HashMap<String, String> namesSkus= new HashMap<String,String>(); 
		
		System.out.println("EASY LIFE MEALS  |  GYM MEALS DIRECT");
		System.out.println();
		System.out.println("loading file input.csv ...");

		try
		{
			BufferedReader input = new BufferedReader(new FileReader("input.csv"));//Buffered Reader object instance with FileReader
			System.out.print("Reading...");
			String fileRead = input.readLine(); // Headers
			fileRead = input.readLine(); //first real line
			
			while (fileRead != null)
			{
				// split input line on commas, except those between quotes ("")
				String[] tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				
				//Fixing lines that are incorrectly ended prematurely due to line break in fields like 'notes'
				// System.out.print("line size = "+tokenize.length+"   ");
				while(tokenize.length<56){
					fileRead = fileRead + input.readLine();
					tokenize = fileRead.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
//					System.out.println("line size = "+tokenize.length);
				}		

				String orderID = tokenize[0];							//OrderID
				String email = tokenize[1];
				String discountCode = tokenize[12];						//coupon that was used
				String shippingMethod = tokenize[14];					//shipping method that was used
				int lineItemQuantity = Integer.parseInt(tokenize[16]);	//quantity of current item
				String lineItemName = tokenize[17];						//product name of current item
				String lineItemSKU = tokenize[20];						//SKU of current item (i.e. GMD-12)
				String billingName = tokenize[24];						//Billing Name provided by customer
				String shippingName = tokenize[34];						//Shipping Name provided by customer
				String shippingAddress1 = tokenize[36];					//Shipping Address provided
				String shippingCity = tokenize[39];						//Shipping city provided (suburb)
				String shippingPostcode = tokenize[40].replaceAll("[\\D]", "");
				String shippingPhone = tokenize[43].replaceAll("[\\D]", "");
				String notes=tokenize[44];								//notes provided by customer regarding shipping
				String vendor=tokenize[50];
				
				//if customer has not ticked shipping is same as billing, and has left shipping blank
				if(shippingName != null && !shippingName.isEmpty()){
					shippingName = billingName;
				}
				//remove apostrophe from postcode from shopify output
//				if(shippingPostcode.contains("'")){
//					shippingPostcode = shippingPostcode.replace("'", "");
//				}
//				//remove special characters from mobile from shopify output
//				if(shippingPhone.contains("'")){
//					shippingPhone = shippingPhone.replace("'", "");
//				}
				
				//indenting protein ball names so they're seperated from the ordinary meals
				if(lineItemName.toLowerCase().contains("protein balls")){
					lineItemName = " " + lineItemName;
				}
				if(lineItemName.toLowerCase().contains("raw bars")){
					lineItemName = "  " + lineItemName;
				}
				if(vendor.toLowerCase().contains("snack")){
					lineItemName = " " + lineItemName;
				}
				if(lineItemName.toLowerCase().contains("pancake")){
					lineItemName = "_" + lineItemName;
				}
				if(lineItemName.toLowerCase().contains("omelette")){
					lineItemName = "__" + lineItemName;
				}
			
				
				//build orderLine object from chosen extracted values
				orderLine.add(
					new OrderItem(orderID, discountCode, shippingMethod, lineItemQuantity, lineItemName,
						lineItemSKU, billingName, shippingAddress1, shippingCity, shippingPostcode, notes, shippingPhone, email, vendor)
				);
				
				//System.out.println("Built Order "+count);
				//System.out.print("reading in new line from file... ");
				fileRead = input.readLine();
				count+=1;
				//System.out.println("line read");
			}

			input.close();
			System.out.println(" " + count + " objects built");
			System.out.println();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println("error: file not found!");
			System.exit(1);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
		
		int giftCardCount=0, orderCount=0;
		int smalls = 0, larges = 0, snacks = 0;
		String oldOrderID = "NOTAREALID";		
		
		// Here we loop through all of our order objects to fully build our hashmaps
		for (OrderItem order : orderLine)
		{
			String sku = order.getLineItemSKU(); //sku of current orderLine
			String name = order.getLineItemName();
			totalQuantity += order.getLineItemQuantity(); //Total items sold counter
			//System.out.println(order.getOrderID());
			//System.out.println(order.getLineItemSKU());

			//Skip to next orderLine object if order is a gift card
			//This is because gift cards don't contain a sku, and as such, give us problems later on if we process it
			//this is because the blank sku isn't really blank, but is "" which cannot be identified easily
			if(order.getLineItemName().toLowerCase().contains("gift card")){
				giftCardCount++;
				continue;
			}
			orderCount++; //Processed orders (gift card orders are NOT processed
			
			if(order.getLineItemName().toLowerCase().contains("large"))
				larges+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("small"))
				smalls+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("red cross"))
				smalls+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("meals on wheels"))
				smalls+=order.getLineItemQuantity();
			if(order.getVendor().toLowerCase().contains("snack") || order.getVendor().toLowerCase().contains("macro"))
				snacks+=order.getLineItemQuantity();
			
			// Storing the different shipping methods and the different orders to each shipping method
			if (!(order.getOrderID().equals(oldOrderID))){
				String shippingMethod = order.getShippingMethod().toLowerCase();
				if (!ordersByShippingMethod.containsKey(shippingMethod)){
					ordersByShippingMethod.put(shippingMethod, new ArrayList<OrderItem>());		
				}
				ordersByShippingMethod.get(shippingMethod).add(order);
				oldOrderID=order.getOrderID();
			}
			
			// Tally order quantities in a HashMap on SKU
			if (skuQuantities.containsKey(sku)){	//if sku already exists in hashMap
				name = order.getLineItemName();
				if (name.equals(skuNames.get(sku))){ //if name matches sku value
					skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
				}			
			} else { //if item does not exist in hashmap yet
				skuQuantities.put(sku, order.getLineItemQuantity()); //Add sku and initial quantity			
				skuList.add(sku); //add sku
				skuNames.put(sku, order.getLineItemName()); //Add sku and name
				
				if(order.getVendor().contains("Gym Meals Direct"))
					namesListGMD.add(name);
				if(order.getVendor().contains("Easy Life Meals"))
					namesListSpecials.add(name);
			}		

					
		}
		System.out.println("Total Meals Count: "+totalQuantity);
		System.out.println("Large Meals Count: "+larges);
		System.out.println("Small Meals Count: "+smalls);
		System.out.println("Snacks Count: "+snacks);
		System.out.println("Gift Cards Count: "+giftCardCount);
		System.out.println(orderCount+"/"+count+" orders processed"); //should be total orders - gift card
		System.out.println();
		
		
		System.out.print("Sorting Meal Names...");
		for(String currentSku: skuList){
			namesSkus.put(skuNames.get(currentSku), currentSku);
			namesList.add(skuNames.get(currentSku));			
		}
		
		skuList.clear();
		Collections.sort(namesList);
		Collections.sort(namesListGMD);
		Collections.sort(namesListSpecials);
		for(String currentName : namesList){
			skuList.add(namesSkus.get(currentName));
		}
		System.out.println(" Done!");


		System.out.print("Printing all meal totals...");
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skuList, larges, smalls, snacks, false, "_meal_totals_FULL.csv");//Prints the totals of each meal
		
		System.out.print("Printing gmd meal totals...");
		skuList.clear();
		for(String currentName : namesListGMD){
			skuList.add(namesSkus.get(currentName));
		}
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skuList, larges, smalls, snacks, false, "_meal_totals_GMD.csv");//Prints the totals of each meal

		System.out.print("Calculating gmd ingredient totals...");
		CalcPrintIngredients(skuQuantities,skuNames, skuList); //Calculate the ingredients required
		
		System.out.print("Printing chef specials meal totals...");
		skuList.clear();
		for(String currentName : namesListSpecials){
			skuList.add(namesSkus.get(currentName));
		}
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skuList, larges, smalls, snacks, true, "_meal_totals_SPECIALS.csv");//Prints the totals of each meal
		
		
		System.out.print("Printing sorted delivery methods...");
		PrintShipping(ordersByShippingMethod); //Print the shipping details of each order of each method
	}

	/*
	 *   Writes the total sold quantities of each menu item
	 *   to file '_meal_totals.csv'
	 */
	public static void PrintMealTotals(HashMap<String, Integer> quantities, HashMap<String, String> skuNames, int total, ArrayList<String> skus, int larges, int smalls, int snacks, boolean skip, String fileName){
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter totals = new BufferedWriter(new FileWriter(fileName, false));
			String mealName;
			String sauceName;
			
			HashMap<String, Integer> sauceTotals = new HashMap<String,Integer>();	//sauce totals
			ArrayList<String> sauces = new ArrayList<String>();
			
			ArrayList<String> itemNames = new ArrayList<String>();
			ArrayList<String> itemSkus = new ArrayList<String>();
			int subtotal = 0;
			int subtotalLarge = 0;
			int subtotalSmall = 0;

			if(!fileName.contains("GMD")){
				totals.write("TOTAL MEALS: "+ (total-snacks));
				totals.newLine();
				totals.newLine();
				totals.newLine();
			}
			
			totals.write("NAME"+","+"TOTAL"+","+"MEAL");
			totals.newLine();
			

			// Write the quantities of each meal to file
			for(String sku : skus){
//				String sku = namesSku.get(name);
				
				itemSkus.add(sku);
				itemNames.add(skuNames.get(sku));
				
				totals.write(skuNames.get(sku)+ "," +quantities.get(sku)+ ","+sku);
				totals.newLine();
				
				subtotal+=quantities.get(sku);
				if(skuNames.get(sku).contains("LARGE"))
					subtotalLarge += quantities.get(sku);
				if(skuNames.get(sku).contains("SMALL") || skuNames.get(sku).contains("Red Cross") || skuNames.get(sku).contains("Meals on Wheels"))
					subtotalSmall += quantities.get(sku);
				
				if(skip)
					continue;								
				
				String[] mealNameSplit = skuNames.get(sku).split(" - ");
				sauceName = mealNameSplit[0];
				
				if (sku.contains("LGE")||sku.contains("SML"))
				{
					if (sauceTotals.containsKey(sauceName)){ //if item already exists in hashMap
						sauceTotals.put(sauceName, sauceTotals.get(sauceName)+quantities.get(sku));					
					}
					else {
						sauceTotals.put(sauceName, quantities.get(sku));
						sauces.add(sauceName);
					}
				}
					
			}
			
			totals.newLine();
			totals.write("SUBTOTAL:"+ ","+(subtotal));

					
			if (!sauceTotals.isEmpty() && !skip){
							
				//Writing the totals for each sauce type
				totals.newLine();	totals.newLine();	totals.newLine();
				totals.write("SAUCE TOTALS"+","+"LITRES"+","+"TOTAL"+","+"% OF MEALS");				
				for (String sauce : sauces){
					totals.newLine();
					totals.write(sauce +","+String.format("%1$,.2f", sauceTotals.get(sauce)*0.12)+" L"+","+sauceTotals.get(sauce)+","+sauceTotals.get(sauce)*100.0/(total-snacks)+"%");
				}
				totals.newLine(); totals.newLine();
				totals.write("Average"+","+","+(total-snacks)/sauces.size()+","+(((total-snacks)/sauces.size())*100.0)/(total-snacks)+"%");
				
			}
			totals.newLine();totals.newLine();
			totals.write(","+"QUANTITY"+","+"%");
			totals.newLine();
			totals.write("Large Meals"+","+subtotalLarge+","+String.format("%1$,.2f", subtotalLarge*100.0/subtotal)+" %");
			totals.newLine();
			totals.write("Small Meals"+","+subtotalSmall+","+String.format("%1$,.2f", subtotalSmall*100.0/subtotal)+" %");
			
			if(fileName.contains("FULL")){
				totals.newLine();
				totals.write("Snacks"+","+snacks+","+String.format("%1$,.2f", snacks*100.0/subtotal)+" %");
			}
				
			totals.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * This method calculates the quantities of each ingredient and prints them to file.
	 * File will be '_ingredients.csv'
	 */
	public static void CalcPrintIngredients(HashMap<String, Integer> quantities, HashMap<String, String> names, ArrayList<String> skus){
		
		List<Ingredient> ingredients = new ArrayList<>();
		HashMap<String, Double> ingredientsRawMultiplier = new HashMap<>();

		ingredients.add(new Ingredient("Chicken"));		int chicken = 0;
		ingredientsRawMultiplier.put("Chicken", 1.291);
		ingredients.add(new Ingredient("Steak"));		int steak = 1;
		ingredientsRawMultiplier.put("Steak", 1.4579);
		ingredients.add(new Ingredient("Mince"));		int mince = 2;
		ingredientsRawMultiplier.put("Mince", 1.3);
		ingredients.add(new Ingredient("Lamb Backstrap"));		int lambBackstrap = 3;
//		ingredientsRawMultiplier.put("Lamb Backstrap", 1.0);
		ingredients.add(new Ingredient("Barramundi"));	int barramundi = 4;
//		ingredientsRawMultiplier.put("Barramundi", 1.0);
		ingredients.add(new Ingredient("Basa"));		int basa = 5;
		ingredientsRawMultiplier.put("Basa", 1.08);
		ingredients.add(new Ingredient("Rice"));		int rice = 6;
		ingredientsRawMultiplier.put("Rice", 0.39);
		ingredients.add(new Ingredient("Sweet Potato"));int sweetPotato = 7;
		ingredientsRawMultiplier.put("Sweet Potato", 1.2);
		ingredients.add(new Ingredient("Veg"));			int veg = 8;
//		ingredientsRawMultiplier.put("Veg", 1.0);
		ingredients.add(new Ingredient("White Potato Mash"));	int mashPotato = 9;
		ingredientsRawMultiplier.put("White Potato Mash", 1.01);
		ingredients.add(new Ingredient("Cous Cous"));	int cousCous = 10;
//		ingredientsRawMultiplier.put("Cous Cous", 1.0);
		ingredients.add(new Ingredient("Tikka Rice"));	int tikka = 11;
		ingredientsRawMultiplier.put("Tikka Rice", 0.39);
		ingredients.add(new Ingredient("White Potato"));		int potato = 12;
		ingredientsRawMultiplier.put("White Potato", 1.28);
		ingredients.add(new Ingredient("Diced Beef"));	int beef = 13;
//		ingredientsRawMultiplier.put("Diced Beef", 1.0);
		ingredients.add(new Ingredient("Brown Rice"));	int brownRice = 14;
		ingredientsRawMultiplier.put("Brown Rice", 0.42735);
		ingredients.add(new Ingredient("Lentils"));		int lentil = 15;
//		ingredientsRawMultiplier.put("Lentils", 1.0);
		ingredients.add(new Ingredient("broccolii"));	int broccolii = 16;
		ingredientsRawMultiplier.put("broccolii", 1.5);
		ingredients.add(new Ingredient("Beans"));		int beans = 17;
		ingredientsRawMultiplier.put("Beans", 1.10);
		ingredients.add(new Ingredient("Peas"));		int peas = 18;
		ingredientsRawMultiplier.put("Peas", 1.09);
		ingredients.add(new Ingredient("Rice Noodles"));int riceNoodles = 19;
//		ingredientsRawMultiplier.put("Rice Noodles", 1.0);
		ingredients.add(new Ingredient("Tofu"));		int tofu = 20;
//		ingredientsRawMultiplier.put("Tofu", 1.0);
		ingredients.add(new Ingredient("Capsicum"));	int capsicum = 21;
		ingredientsRawMultiplier.put("Capsicum", 1.30);
		ingredients.add(new Ingredient("Carrot"));		int carrot = 22;
		ingredientsRawMultiplier.put("Carrot", 1.12);
		ingredients.add(new Ingredient("Mushroom"));	int mushroom = 23;
//		ingredientsRawMultiplier.put("Mushroom", 1.0);
		ingredients.add(new Ingredient("Risotto Rice"));int risottoRice = 24;
		ingredientsRawMultiplier.put("Risotto Rice", 0.33);
		ingredients.add(new Ingredient("Quinoa"));		int quinoa = 25;
//		ingredientsRawMultiplier.put("Quinoa", 1.0);
		ingredients.add(new Ingredient("Corn"));		int corn = 26;
//		ingredientsRawMultiplier.put("Corn", 1.0);
		ingredients.add(new Ingredient("Sweet Potato Mash"));		int mashSweetPotato = 27;
//		ingredientsRawMultiplier.put("Sweet Potato Mash", 1.0);
		ingredients.add(new Ingredient("Cauliflower Rice"));	int cauliflower = 28;
		ingredientsRawMultiplier.put("Cauliflower Rice", 1.08);
		ingredients.add(new Ingredient("Zuchini"));		int zuchini = 29;
		ingredientsRawMultiplier.put("Zuchini", 1.15);
		ingredients.add(new Ingredient("Eggplant"));	int eggplant = 30;
		ingredientsRawMultiplier.put("Eggplant", 1.5151);
		ingredients.add(new Ingredient("Tenderloin"));		int tenderloin = 31;
		ingredientsRawMultiplier.put("Tenderloin", 1.29);
		ingredients.add(new Ingredient("Lamb Shoulder"));		int lambShoulder = 32;
		ingredientsRawMultiplier.put("Lamb Shoulder", 1.48);
		ingredients.add(new Ingredient("Pumpkin"));		int pumpkin = 33;
		ingredientsRawMultiplier.put("Pumpkin", 1.19);
		ingredients.add(new Ingredient("Penne"));		int penne = 34;
		ingredientsRawMultiplier.put("Penne", 0.47);
		ingredients.add(new Ingredient("Wholemeal Penne"));		int wholemealPenne = 35;
		ingredientsRawMultiplier.put("Wholemeal Penne", 0.47);
		ingredients.add(new Ingredient("Spaghetti"));		int spaghetti = 36;
		ingredientsRawMultiplier.put("Spaghetti", 0.44);
		ingredients.add(new Ingredient("Vegetable Rice"));		int vegeRice = 37;
		ingredientsRawMultiplier.put("Vegetable Rice", 0.39);
		ingredients.add(new Ingredient("Broccoli Rice"));		int broccoliRice = 38;
		ingredientsRawMultiplier.put("Broccoli Rice", 1.1);
		ingredients.add(new Ingredient("Red Capsicum"));	int capsicumRed = 39;
		ingredientsRawMultiplier.put("Red Capsicum", 1.30);
		ingredients.add(new Ingredient("Chick Peas"));	int chickPeas = 40;
		ingredientsRawMultiplier.put("Chick Peas", 1.0);
		ingredients.add(new Ingredient("Soba Noodles"));	int soba = 41;
		ingredientsRawMultiplier.put("Soba Noodles", 0.50);
		ingredients.add(new Ingredient("Shredded Carrot"));	int shreddedCarrot = 42;
		ingredientsRawMultiplier.put("Shredded Carrot", 1.12);
		ingredients.add(new Ingredient("Chicken Mince"));	int chickenMince = 43;
		ingredientsRawMultiplier.put("Chicken Mince", 1.3);

		/*
		 * Totalling Ingredient Quantities
		 * This is accomplished by searching the menu item name for keywords like large, chicken, rice, etc
		 * i.e 'Pepperberry Steak - Large/Rice' would add 200g to the beef and rice quantities
		 */
		for(String sku : skus){
			String tempName = names.get(sku).toLowerCase();

			if(tempName.contains("supabarn")){
				ingredients.get(chicken).addQuantity(quantities.get(sku), 100);
			}
			if(tempName.contains("large")){
				if(tempName.contains("chicken"))
					ingredients.get(chicken).addQuantity(quantities.get(sku), 150);
				if(tempName.contains("steak"))
					ingredients.get(steak).addQuantity(quantities.get(sku), 150);
				if(tempName.contains("italian meatballs")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 150);
					ingredients.get(wholemealPenne).addQuantity(quantities.get(sku), 250);
				}
				if(tempName.contains("con carne")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 150);
					ingredients.get(rice).addQuantity(quantities.get(sku), 200);
				}
				if(tempName.contains("barra"))
					ingredients.get(barramundi).addQuantity(quantities.get(sku), 160);
				if(tempName.contains("basa"))
					ingredients.get(basa).addQuantity(quantities.get(sku), 150);
				if(tempName.contains("moroccan lamb"))
					ingredients.get(lambBackstrap).addQuantity(quantities.get(sku), 150);
				else if(tempName.contains("slow roasted lamb")){
					ingredients.get(lambShoulder).addQuantity(quantities.get(sku), 150);
				}
				if(tempName.contains("cottage pie")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 150);
					ingredients.get(mashPotato).addQuantity(quantities.get(sku), 150);
				}
				if(tempName.contains("beef goulash")){
					ingredients.get(steak).addQuantity(quantities.get(sku), 150);
					ingredients.get(potato).addQuantity(quantities.get(sku), 150);
				}
				if(tempName.contains("coconut curry lentils")){
					ingredients.get(lentil).addQuantity(quantities.get(sku), 150);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 40);
					ingredients.get(peas).addQuantity(quantities.get(sku), 40);
				}
				if(tempName.contains("tofu pad thai")){
					ingredients.get(riceNoodles).addQuantity(quantities.get(sku), 100);
					ingredients.get(tofu).addQuantity(quantities.get(sku), 100);
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 80);
					ingredients.get(carrot).addQuantity(quantities.get(sku), 20);
					ingredients.get(beans).addQuantity(quantities.get(sku), 40);
				}
				if(tempName.contains("mushroom quinoa risotto")){
					ingredients.get(mushroom).addQuantity(quantities.get(sku), 50);
					ingredients.get(risottoRice).addQuantity(quantities.get(sku), 90);
					ingredients.get(peas).addQuantity(quantities.get(sku), 50);
					ingredients.get(quinoa).addQuantity(quantities.get(sku), 20);
					ingredients.get(beans).addQuantity(quantities.get(sku), 30);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 80);
				}
				if(tempName.contains("burrito bowl")){
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 80);
					ingredients.get(quinoa).addQuantity(quantities.get(sku), 150);
					ingredients.get(beans).addQuantity(quantities.get(sku), 120);
					ingredients.get(corn).addQuantity(quantities.get(sku), 25);
				}
				if(tempName.contains("thai green curry")){
					ingredients.get(chicken).addQuantity(quantities.get(sku), 150);
					ingredients.get(cauliflower).addQuantity(quantities.get(sku), 200);
				}
				if(tempName.contains("sunday roast chicken")){
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 50);
					ingredients.get(zuchini).addQuantity(quantities.get(sku), 50);
					ingredients.get(eggplant).addQuantity(quantities.get(sku), 50);
					ingredients.get(carrot).addQuantity(quantities.get(sku), 50);
				}
				if(tempName.contains("lemon chicken")){
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 50);
					ingredients.get(zuchini).addQuantity(quantities.get(sku), 50);
					ingredients.get(eggplant).addQuantity(quantities.get(sku), 50);
					ingredients.get(carrot).addQuantity(quantities.get(sku), 50);
				}
				if(tempName.contains("pesto penne")){
					ingredients.get(penne).addQuantity(quantities.get(sku), 350);
				}
				if(tempName.contains("spaghetti bolognese")){
					ingredients.get(spaghetti).addQuantity(quantities.get(sku), 350);
					ingredients.get(mince).addQuantity(quantities.get(sku), 150);
				}
				if((tempName.contains("supabarn"))&&(tempName.contains("coconut curry"))&&(!tempName.contains("chicken"))){
					ingredients.get(chicken).addQuantity(quantities.get(sku), 150);
				}
				if(tempName.contains("honey soy chicken")){
					ingredients.get(vegeRice).addQuantity(quantities.get(sku), 200);
				}
				if((tempName.contains("sweet chilli"))&&(tempName.contains("lime chicken"))){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 100);
					ingredients.get(broccoliRice).addQuantity(quantities.get(sku), 100);
				}
				if((tempName.contains("cajun chicken"))&&(tempName.contains("cauli"))){
					ingredients.get(cauliflower).addQuantity(quantities.get(sku), 100);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 70);
					ingredients.get(peas).addQuantity(quantities.get(sku), 30);
				}
				if((tempName.contains("cajun chicken"))&&(tempName.contains("cous"))){
					ingredients.get(cousCous).addQuantity(quantities.get(sku), 100);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 70);
					ingredients.get(peas).addQuantity(quantities.get(sku), 30);
				}
				if(tempName.contains("broccoli tabouli")){
					ingredients.get(broccoliRice).addQuantity(quantities.get(sku), 125);
					ingredients.get(quinoa).addQuantity(quantities.get(sku), 40);
					ingredients.get(capsicumRed).addQuantity(quantities.get(sku), 20);
					ingredients.get(chickPeas).addQuantity(quantities.get(sku), 40);
				}
				if(tempName.contains("parmesan & herb")){
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
					ingredients.get(zuchini).addQuantity(quantities.get(sku), 120);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 120);
				}
				if(tempName.contains("oriental chicken")){
					ingredients.get(soba).addQuantity(quantities.get(sku), 200);
					ingredients.get(shreddedCarrot).addQuantity(quantities.get(sku), 50);
				}
				if(tempName.contains("rosemary roast")){
					ingredients.get(mashPotato).addQuantity(quantities.get(sku), 150);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 75);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				if(tempName.contains("chicken & spinach meatballs")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 150);
					ingredients.get(wholemealPenne).addQuantity(quantities.get(sku), 250);
					ingredients.get(chicken).addQuantity(quantities.get(sku), -150);
				}
				if(tempName.contains("teriyaki steak soba")){
					ingredients.get(soba).addQuantity(quantities.get(sku), 250);
				}
				
				
				if((tempName.contains("veg"))&&(tempName.contains("brown rice"))){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 100);
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 100);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if((tempName.contains("veg"))&&(tempName.contains("rice"))){
					ingredients.get(rice).addQuantity(quantities.get(sku), 100);
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 100);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if((tempName.contains("veg"))&&(tempName.contains("sweet potato"))){
					ingredients.get(sweetPotato).addQuantity(quantities.get(sku), 100);
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 100);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if((tempName.contains("sweet potato mash"))&&(tempName.contains("broccoli"))){
					ingredients.get(mashSweetPotato).addQuantity(quantities.get(sku), 150);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 100);
				}
				else if((tempName.contains("pumpkin"))&&(tempName.contains("cauliflower"))&&(tempName.contains("brown rice"))){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 150);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 50);
					ingredients.get(cauliflower).addQuantity(quantities.get(sku), 50);
				}
				else if(tempName.contains("salsa")){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 60);
				}
				else if(tempName.contains("fajita"))
					ingredients.get(rice).addQuantity(quantities.get(sku), 150);
				else if(tempName.contains("tikka"))
					ingredients.get(tikka).addQuantity(quantities.get(sku), 200);
				else if(tempName.contains("tuscan")){
					ingredients.get(potato).addQuantity(quantities.get(sku), 100);
					ingredients.get(sweetPotato).addQuantity(quantities.get(sku), 100);
				}
				else if(tempName.contains("rice"))
					ingredients.get(rice).addQuantity(quantities.get(sku), 200);
				else if(tempName.contains("veg"))
				{
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 180);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 140);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if(tempName.contains("sweet potato"))
					ingredients.get(sweetPotato).addQuantity(quantities.get(sku), 200);

			}
			if(tempName.contains("small") || tempName.contains("medium")){
				if(tempName.contains("chicken"))
					ingredients.get(chicken).addQuantity(quantities.get(sku), 100);
				if(tempName.contains("steak"))
					ingredients.get(steak).addQuantity(quantities.get(sku), 100);
				if(tempName.contains("italian meatballs")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 100);
					ingredients.get(wholemealPenne).addQuantity(quantities.get(sku), 200);
				}
				if(tempName.contains("con carne")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 100);
					ingredients.get(rice).addQuantity(quantities.get(sku), 150);
				}
				if(tempName.contains("barra"))
					ingredients.get(barramundi).addQuantity(quantities.get(sku), 100);
				if(tempName.contains("basa"))
					ingredients.get(basa).addQuantity(quantities.get(sku), 100);
				if(tempName.contains("moroccan lamb"))
					ingredients.get(lambBackstrap).addQuantity(quantities.get(sku), 100);
				else if(tempName.contains("slow roasted lamb")){
					ingredients.get(lambShoulder).addQuantity(quantities.get(sku), 100);
				}
				if(tempName.contains("cottage pie")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 100);
					ingredients.get(mashPotato).addQuantity(quantities.get(sku), 150);
				}
				if(tempName.contains("beef goulash")){
					ingredients.get(steak).addQuantity(quantities.get(sku), 100);
					ingredients.get(potato).addQuantity(quantities.get(sku), 100);
				}
				if(tempName.contains("coconut curry lentils")){
					ingredients.get(lentil).addQuantity(quantities.get(sku), 120);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 60);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);

				}
				if(tempName.contains("tofu pad thai")){
					ingredients.get(riceNoodles).addQuantity(quantities.get(sku), 80);
					ingredients.get(tofu).addQuantity(quantities.get(sku), 80);
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 40);
					ingredients.get(carrot).addQuantity(quantities.get(sku), 15);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
				}
				if(tempName.contains("mushroom quinoa risotto")){
					ingredients.get(mushroom).addQuantity(quantities.get(sku), 30);
					ingredients.get(risottoRice).addQuantity(quantities.get(sku), 70);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
					ingredients.get(quinoa).addQuantity(quantities.get(sku), 20);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 60);
				}
				if(tempName.contains("burrito bowl")){
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 60);
					ingredients.get(quinoa).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 80);
					ingredients.get(corn).addQuantity(quantities.get(sku), 20);

				}
				if(tempName.contains("thai green curry")){
					ingredients.get(chicken).addQuantity(quantities.get(sku), 100);
					ingredients.get(cauliflower).addQuantity(quantities.get(sku), 120);
				}
				if(tempName.contains("sunday roast chicken")){
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 40);
					ingredients.get(zuchini).addQuantity(quantities.get(sku), 40);
					ingredients.get(eggplant).addQuantity(quantities.get(sku), 40);
					ingredients.get(carrot).addQuantity(quantities.get(sku), 40);
				}
				if(tempName.contains("lemon chicken")){
					ingredients.get(capsicum).addQuantity(quantities.get(sku), 40);
					ingredients.get(zuchini).addQuantity(quantities.get(sku), 40);
					ingredients.get(eggplant).addQuantity(quantities.get(sku), 40);
					ingredients.get(carrot).addQuantity(quantities.get(sku), 40);
				}
				if(tempName.contains("pesto penne")){
					ingredients.get(penne).addQuantity(quantities.get(sku), 250);
				}
				if(tempName.contains("spaghetti bolognese")){
					ingredients.get(spaghetti).addQuantity(quantities.get(sku), 250);
					ingredients.get(mince).addQuantity(quantities.get(sku), 100);
				}
				if((tempName.contains("supabarn"))&&(tempName.contains("coconut curry"))&&(!tempName.contains("chicken"))){
					ingredients.get(chicken).addQuantity(quantities.get(sku), 100);
				}
				if(tempName.contains("honey soy chicken")){
					ingredients.get(vegeRice).addQuantity(quantities.get(sku), 150);
				}
				if((tempName.contains("sweet chilli"))&&(tempName.contains("lime chicken"))){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 75);
					ingredients.get(broccoliRice).addQuantity(quantities.get(sku), 75);
				}
				if((tempName.contains("cajun chicken"))&&(tempName.contains("cauli"))){
					ingredients.get(cauliflower).addQuantity(quantities.get(sku), 75);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 50);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				if((tempName.contains("cajun chicken"))&&(tempName.contains("cous"))){
					ingredients.get(cousCous).addQuantity(quantities.get(sku), 75);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 50);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				if(tempName.contains("broccoli tabouli")){
					ingredients.get(broccoliRice).addQuantity(quantities.get(sku), 100);
					ingredients.get(quinoa).addQuantity(quantities.get(sku), 30);
					ingredients.get(capsicumRed).addQuantity(quantities.get(sku), 15);
					ingredients.get(chickPeas).addQuantity(quantities.get(sku), 30);
				}
				if(tempName.contains("parmesan & herb")){
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
					ingredients.get(zuchini).addQuantity(quantities.get(sku), 80);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 80);
				}
				if(tempName.contains("oriental chicken")){
					ingredients.get(soba).addQuantity(quantities.get(sku), 160);
					ingredients.get(shreddedCarrot).addQuantity(quantities.get(sku), 40);
				}
				if(tempName.contains("lasagne")){
					ingredients.get(chickenMince).addQuantity(quantities.get(sku), 50);
				}
				if(tempName.contains("rosemary roast")){
					ingredients.get(mashPotato).addQuantity(quantities.get(sku), 150);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 75);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				if(tempName.contains("chicken & spinach meatballs")){
					ingredients.get(chickenMince).addQuantity(quantities.get(sku), 100);
					ingredients.get(wholemealPenne).addQuantity(quantities.get(sku), 200);
					ingredients.get(chicken).addQuantity(quantities.get(sku), -100);
				}
				if(tempName.contains("teriyaki steak soba")){
					ingredients.get(soba).addQuantity(quantities.get(sku), 200);
				}


				if((tempName.contains("veg"))&&(tempName.contains("brown rice"))){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 75);
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 70);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 75);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if((tempName.contains("veg"))&&(tempName.contains("rice"))){
					ingredients.get(rice).addQuantity(quantities.get(sku), 75);
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 70);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 75);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if((tempName.contains("veg"))&&(tempName.contains("sweet potato"))){
					ingredients.get(sweetPotato).addQuantity(quantities.get(sku), 75);
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 70);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 75);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if((tempName.contains("sweet potato mash"))&&(tempName.contains("broccoli"))){
					ingredients.get(mashSweetPotato).addQuantity(quantities.get(sku), 60);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 60);
				}
				else if((tempName.contains("pumpkin"))&&(tempName.contains("cauliflower"))&&(tempName.contains("brown rice"))){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 75);
					ingredients.get(pumpkin).addQuantity(quantities.get(sku), 40);
					ingredients.get(cauliflower).addQuantity(quantities.get(sku), 35);
				}
				else if(tempName.contains("salsa")){
					ingredients.get(brownRice).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 40);
				}

				else if(tempName.contains("fajita"))
					ingredients.get(rice).addQuantity(quantities.get(sku), 100);
				else if(tempName.contains("tikka"))
					ingredients.get(tikka).addQuantity(quantities.get(sku), 120);
				else if(tempName.contains("tuscan")){
					ingredients.get(potato).addQuantity(quantities.get(sku), 75);
					ingredients.get(sweetPotato).addQuantity(quantities.get(sku), 75);
				}
				else if(tempName.contains("rice"))
					ingredients.get(rice).addQuantity(quantities.get(sku), 120);
				else if(tempName.contains("veg")){
					//					ingredients.get(veg).addQuantity(quantities.get(sku), 100);
					ingredients.get(broccolii).addQuantity(quantities.get(sku), 100);
					ingredients.get(beans).addQuantity(quantities.get(sku), 20);
					ingredients.get(peas).addQuantity(quantities.get(sku), 20);
				}
				else if(tempName.contains("sweet potato"))
					ingredients.get(sweetPotato).addQuantity(quantities.get(sku), 150);
			}
		}
		

		// Writing ingredient quantities to file
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter ingredientsFile = new BufferedWriter(new FileWriter("_ingredients.csv", false));

			ingredientsFile.write("INGREDIENT"+","+"COOKED (kg)"+","+"RAW (kg)");
			ingredientsFile.newLine();

			for (Ingredient ingri : ingredients)
			{
				if(ingri.getQuantity() == 0)
					continue;
				
				ingredientsFile.newLine();
				ingredientsFile.write(ingri.getName()+","+(float)ingri.getQuantity()/1000);
				if(ingredientsRawMultiplier.containsKey(ingri.getName())){
					ingredientsFile.write(","+((float)ingri.getQuantity()*ingredientsRawMultiplier.get(ingri.getName()))/1000);
				}
			}

			ingredientsFile.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * Prints the orders of each shipping method
	 */
	public static void PrintShipping(HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod){

		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter shipping = new BufferedWriter(new FileWriter("_shipping.csv", false));
			BufferedWriter notes = new BufferedWriter(new FileWriter("_delivery_notes.csv", false));
			BufferedWriter deliveries = new BufferedWriter(new FileWriter("_deliveries.csv", false));
			BufferedWriter pickupTables = new BufferedWriter(new FileWriter("_pickup_tables.csv", false));
			
			shipping.write("SHIPPING METHODS: " + ordersByShippingMethod.keySet().size());
			shipping.newLine();	shipping.newLine();	shipping.newLine();	shipping.newLine();	shipping.newLine();
			
			notes.write("NAME,ADDRESS,PHONE");
			notes.newLine();
			notes.write("EMAIL,NOTE,METHOD");
			notes.newLine();notes.newLine();notes.newLine();
			
			deliveries.write("Order ID"+","+"Shipping Name"+","+"Shipping Street"+","+"Shipping City"+","+"Postcode"+","+"Mobile"+","+"Notes");
			deliveries.newLine();
			
			ArrayList<Character> chars = new ArrayList<>();
			HashMap<Character, Integer> charCounts = new HashMap<>();
			int pickupTotal = 0;

			//Looping through all shipping methods and within that, looping through all orders while printing
			for(String shippingMethod : ordersByShippingMethod.keySet()){
				shipping.write("     METHOD,     NAME,     ADDRESS,     NOTES,     ORDER ID");
				shipping.newLine();
				for(OrderItem order : ordersByShippingMethod.get(shippingMethod)){
					String shippingString = order.getShippingMethod() + "," + order.getShippingName() + "," + order.getShippingAddress() + ",";
					shippingString = shippingString+order.getNotes()+","+order.getOrderID();				
					shipping.write(shippingString);
					shipping.newLine();
					
					if(shippingMethod.toLowerCase().contains("delivery")){
						if(order.getNotes() != null && !order.getNotes().isEmpty()){
							notes.write(order.getShippingName()+","+order.getShippingAddress()+","+order.getShippingPhone());
							notes.newLine();
							notes.write(order.getEmail()+","+order.getNotes()+","+shippingMethod);
							notes.newLine();notes.newLine();notes.newLine();
						}
						
						deliveries.write(order.getFullShippingString());
						deliveries.newLine();
					}
					
					if(shippingMethod.toLowerCase().contains("pick up")){
						pickupTotal++;
						String pickupName = (order.getShippingName()).toUpperCase();
						int index = 0;
						char surnameInitial = '_';
						if(pickupName != null && !pickupName.isEmpty() && pickupName.contains(" ") &&  pickupName.lastIndexOf(' ') != pickupName.length()-1){
							index = pickupName.lastIndexOf(' ') +1;
//							if(index+1 == pickupName.length() && pickupName.length() > 1)
//								index--;
							surnameInitial = pickupName.charAt(index);
						}
						
						if(!chars.contains(surnameInitial)){
							chars.add(surnameInitial);
							charCounts.put(surnameInitial, 0);
						}
						charCounts.put(surnameInitial, charCounts.get(surnameInitial)+1);
//						System.out.println(pickupName + " -- " + surnameInitial);
					}

				}
				
				shipping.write("     TOTAL: " + ordersByShippingMethod.get(shippingMethod).size());
				shipping.newLine();	shipping.newLine();	shipping.newLine();
			}
			Collections.sort(chars);
			
			String tableString = "";
			int tables = 7;
			int boxesPerTable = pickupTotal / tables;
//			System.out.println(boxesPerTable);
			int currentTableCount = 0;
			
			pickupTables.write("Total Pickups"+","+pickupTotal);
			pickupTables.newLine();
			pickupTables.write("Min per table"+","+boxesPerTable);
			pickupTables.newLine();
			pickupTables.newLine();
			pickupTables.write("CHARACTERS"+","+"TOTAL");
			pickupTables.newLine();
			
			for (char character: chars){ 
//				System.out.println(character);
				currentTableCount += charCounts.get(character);
				tableString += character;
				
				if(currentTableCount >= boxesPerTable){
					pickupTables.write(tableString + "," + currentTableCount);
					pickupTables.newLine();
					currentTableCount = 0;
					tableString = "";
				}
				
			}
			pickupTables.close();

			deliveries.close();
			shipping.close();
			notes.close();
			System.out.println(" Done!");
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			System.exit(0);
		}

	}
}
