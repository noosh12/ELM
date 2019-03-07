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
		HashMap<String, String> reverseNames = new HashMap<String, String>();
		HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod = new HashMap<String, ArrayList<OrderItem>> (); // Orders by shipping method
		
		ArrayList<String> skus = new ArrayList<String>(); //Arraylist that holds skus - for sorting
		ArrayList<String> names = new ArrayList<String>(); //Arraylist that holds names - for sorting
		HashMap<String, String> Names= new HashMap<String,String>(); //Arraylist for sorted Names
		
		ArrayList<String> orderedOld = new ArrayList<String>(); //order IDs of orders that contain old/discontinued items
		ArrayList<Integer> skusOld = new ArrayList<Integer>(); //skus of old/discontinued items
		ArrayList<String> skusOldNames = new ArrayList<String>(); //skus of old/discontinued items
		
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
					System.out.println("line size = "+tokenize.length);
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
			
				
				//build orderLine object from chosen extracted values
				orderLine.add(
					new OrderItem(orderID, discountCode, shippingMethod, lineItemQuantity, lineItemName,
						lineItemSKU, billingName, shippingAddress1, shippingCity, shippingPostcode, notes, shippingPhone, email)
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
		
		//Preliminary run to build hashmaps skipping first 1/4 of lines
		//This is because the first and last lines could be problematic
		//if customer ordered before/after menu changed
		for (OrderItem order : orderLine)
		{
			if (orderCount>count/4){ 
				//skipping first quarter of lines in case customer ordered before menu changed
				
				//Skip to next orderLine object if order is a gift card
				//This is because gift cards don't contain a sku, and as such, give us problems later on if we process it
				//this is because the blank sku isn't really blank, but is "" which cannot be identified easily
				if(order.getLineItemName().toLowerCase().contains("gift card")){
					giftCardCount++;
					continue;
				}
				
				String sku = order.getLineItemSKU();
				String name = order.getLineItemName();
				if(!reverseNames.containsKey(name)){
					if(!skuNames.containsKey(sku)){
						skuNames.put(sku, name);
						skuQuantities.put(sku, 0);
						skus.add(sku);
						reverseNames.put(name, sku);
					}
				}
			}
			orderCount++;	
		}
		System.out.println("Preliminary run completed on line "+count/4+" to "+orderCount);
		
		orderCount=0;
		
		// Here we loop through all of our order objects to fully build our hashmaps
		for (OrderItem order : orderLine)
		{
			String sku = order.getLineItemSKU(); //sku of current orderLine
			String name = order.getLineItemName();
			int extraSku;
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
			if(order.getLineItemName().toLowerCase().contains("protein ball"))
				snacks+=order.getLineItemQuantity();
			if(order.getLineItemName().toLowerCase().contains("raw bars"))
				snacks+=order.getLineItemQuantity();

			
			
			// Tally order quantities in a HashMap on SKU
			if (skuQuantities.containsKey(sku)){	//if sku already exists in hashMap
				name = order.getLineItemName();
				if (name.equals(skuNames.get(sku))){ //if name matches sku value
					skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
				}
				else if (reverseNames.containsKey(name)){
					//if hashmap contains this name under a dif sku (extremely rare)
					//occurs when someone orders in between weekly cutoff and menu changed (skus+names)
					//this part covers the use case when the intended sku is already in-use
					//(Part 1 of 2)
					sku = reverseNames.get(name); //set sku to the recorded sku of that menu item
					skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());					
				}
				else{ 
					//if name does not match recorded sku value (less rare) and item name not in hashmap already
					//happens when customer manually orders items not on current menu

					extraSku = Integer.parseInt(sku.replaceAll("[\\D]", "")); //extract integer component of sku
					if(extraSku<10)
					{
						sku = "OLD-0"+extraSku; //duplicate sku item changes prefix to ZZZ i.e GMD-12 -> OLD-12
					}
					else{
						sku = "OLD-"+extraSku; //duplicate sku item changes prefix to ZZZ i.e GMD-12 -> OLD-12
					}
					
					if(!skusOld.contains(extraSku)){
						skusOld.add(extraSku);
						skusOldNames.add(name);
					}
					
					
//					if(skuQuantities.containsKey(sku)){
//						skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
//					}
//					else{
//						skuQuantities.put(sku, order.getLineItemQuantity());
//						skuNames.put(sku, order.getLineItemName());
//						skus.add(sku);
//						
//					}
					//Shouldn't need the above because items that already exist should be caught in the above else if
					skuQuantities.put(sku, order.getLineItemQuantity());
					skuNames.put(sku, order.getLineItemName());
					skus.add(sku);
					reverseNames.put(order.getLineItemName(), sku); //add name and sku
					
					//orderedOld.add(order.getOrderID()); //Recording order ID of current order
				}				
			} else if (reverseNames.containsKey(name)){ //hashmaps contain name but not line sku
				
				sku = reverseNames.get(name); //set sku to the recorded sku of that menu item
				skuQuantities.put(sku, skuQuantities.get(sku) + order.getLineItemQuantity());
				
			} else { //if item does not exist in hashmap yet
				
				skuQuantities.put(sku, order.getLineItemQuantity()); //Add sku and initial quantity
				skuNames.put(sku, order.getLineItemName()); //Add sku and name
				skus.add(sku); //add sku
				reverseNames.put(order.getLineItemName(), sku); //add name and sku
			}		

			// Storing the different shipping methods and the different orders to each shipping method
			if (!(order.getOrderID().equals(oldOrderID))){
				String shippingMethod = order.getShippingMethod().toLowerCase();
				if (!ordersByShippingMethod.containsKey(shippingMethod)){
					ordersByShippingMethod.put(shippingMethod, new ArrayList<OrderItem>());		
				}
				ordersByShippingMethod.get(shippingMethod).add(order);
				oldOrderID=order.getOrderID();
			}		
		}
		System.out.println("Large Meals Count: "+larges);
		System.out.println("Small Meals Count: "+smalls);
		System.out.println("Snacks Count: "+snacks);
		System.out.println("Gift Cards Count: "+giftCardCount);
		System.out.println(orderCount+"/"+count+" orders processed"); //should be total orders - gift card
		System.out.println();
		
//		System.out.println();
//		for(String sku : skus){
//			System.out.println(gmdNames.get(sku)+" - "+gmdQuantities.get(sku));
//		}
//		System.out.println();
		
		//If hashmaps contain old meals, looping through them to determine the real old
		//the sku with the lowest quantity is determined to be the correct old item
		//i.e. if GMD-12 quantity = 10, and OLD-12 has quantity of 100, skus are swapped
		if (!skusOld.isEmpty()){
			System.out.print("Orders contains OLD meals! Determining true OLD meals...");
			for (Integer sku : skusOld){
				String gmdSku="", oldSku="";
				
				if(sku<10)
				{
					gmdSku="GMD-0"+sku;
					oldSku="OLD-0"+sku;
				}
				else{
					gmdSku="GMD-"+sku;
					oldSku="OLD-"+sku;
				}
				
				int gmdQuantity = skuQuantities.get(gmdSku);
				int oldQuantity = skuQuantities.get(oldSku);
				String gmdName = skuNames.get(gmdSku);
				String oldName = skuNames.get(oldSku);
//				System.out.println("current");
//				System.out.println(gmdSku+": "+gmdName+" - "+gmdQuantity);
//				System.out.println(oldSku+": "+oldName+" - "+oldQuantity);						
				
				if (gmdQuantity<oldQuantity){ //Old is the real gmd
					skuQuantities.remove(gmdSku);
					skuNames.remove(gmdSku);
					skuQuantities.remove(oldSku);
					skuNames.remove(oldSku);

					skuQuantities.put(gmdSku, oldQuantity);//making old the real gmd
					skuNames.put(gmdSku, oldName);
					
					skuQuantities.put(oldSku, gmdQuantity);//making gmd the real old
					skuNames.put(oldSku, gmdName);
//					System.out.println("new");
//					System.out.println(gmdSku+": "+oldName+" - "+oldQuantity);
//					System.out.println(oldSku+": "+gmdName+" - "+gmdQuantity);				
					
					if(!skusOldNames.contains(gmdName)){
						skusOldNames.add(gmdName);
					}
					skusOldNames.remove(oldName);
					
				}	
			}
			System.out.println(" Done!");
		}
		
		
		System.out.print("Finding Orders containing OLD meals or mismatched SKU meals... ");
		for (OrderItem order : orderLine){
			if(skusOldNames.contains(order.getLineItemName())){
				if(!orderedOld.contains(order.getOrderID())){
					orderedOld.add(order.getOrderID());
				}
			}
			if(!(order.getLineItemName().equals(skuNames.get(order.getLineItemSKU())))){
				if(!(order.getNotes().contains("**DIF SKUs**"))){
					order.setNotes("**DIF SKUs**"+ order.getNotes());
				}
			}
				
		}
		System.out.println(" Done!");
		
		System.out.print("Sorting Meal Names...");
		for(String currentSku: skus){
			Names.put(skuNames.get(currentSku), currentSku);
			names.add(skuNames.get(currentSku));			
		}
		Collections.sort(names);
		skus.clear();
		for(String currentName : names){
			skus.add(Names.get(currentName));
		}
		System.out.println(" Done!");


		System.out.print("Calculating meal totals...");
		PrintMealTotals(skuQuantities,skuNames,totalQuantity, skus, larges, smalls, snacks);//Prints the totals of each meal

		System.out.print("Calculating ingredient totals...");
		CalcPrintIngredients(skuQuantities,skuNames); //Calculate the ingredients required
		
		System.out.print("Printing sorted delivery methods...");
		PrintShipping(ordersByShippingMethod, orderedOld); //Print the shipping details of each order of each method
	}

	/*
	 *   Writes the total sold quantities of each menu item
	 *   to file '_meal_totals.csv'
	 */
	public static void PrintMealTotals(HashMap<String, Integer> quantities, HashMap<String, String> names, int total, ArrayList<String> skus, int larges, int smalls, int snacks){
		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter totals = new BufferedWriter(new FileWriter("_meal_totals.csv", false));
			String mealName;
			String sauceName;
			boolean duplicates = false;			
			
			int[] typeTotals = new int[20];
			HashMap<String, Integer> sauceTotals = new HashMap<String,Integer>();	//sauce totals
			ArrayList<String> sauces = new ArrayList<String>();
			
			ArrayList<String> itemNames = new ArrayList<String>();
			ArrayList<String> itemSkus = new ArrayList<String>();

			totals.write("TOTAL MEALS: "+ (total-snacks));
			totals.newLine();
			totals.newLine();
			totals.newLine();
			totals.write("NAME"+","+"TOTAL"+","+"MEAL");
			totals.newLine();

			// Write the quantities of each meal to file
			for(String sku : skus){
				
				if (itemSkus.contains(sku)||itemNames.contains(names.get(sku)))
					duplicates = true;
				
					
				itemSkus.add(sku);
				itemNames.add(names.get(sku));
				
				totals.write(names.get(sku)+ "," +quantities.get(sku)+ ","+sku);
				totals.newLine();
				
				
				//if (sku.contains("GMD")||sku.contains("OLD")){
					//Totaling the totals for each meal type
					mealName = names.get(sku).toLowerCase();
					if (mealName.contains("steak")){
						if((mealName.contains("brown rice"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[12]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[13]+=quantities.get(sku);
						}
						else if((mealName.contains("sweet potato"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[16]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[17]+=quantities.get(sku);
						}
						else if(mealName.contains("rice")&&(!mealName.contains("brown"))&&(!mealName.contains("noodles"))){
							if(mealName.contains("large"))
								typeTotals[0]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[1]+=quantities.get(sku);
						}
						else if(mealName.contains("sweet potato")&&(!mealName.contains("mash"))){
							if(mealName.contains("large"))
								typeTotals[2]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[3]+=quantities.get(sku);
						}
						else if(mealName.contains("veg")){
							if(mealName.contains("large"))
								typeTotals[4]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[5]+=quantities.get(sku);
						}
					}
					if (mealName.contains("chicken")){
						if((mealName.contains("brown rice"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[14]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[15]+=quantities.get(sku);
						}
						else if((mealName.contains("sweet potato"))&&(mealName.contains("veg"))){
							if(mealName.contains("large"))
								typeTotals[18]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[19]+=quantities.get(sku);
						}
						else if(mealName.contains("rice")&&(!mealName.contains("brown"))&&(!mealName.contains("noodles"))){
							if(mealName.contains("large"))
								typeTotals[6]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[7]+=quantities.get(sku);
						}
						else if(mealName.contains("sweet potato")&&(!mealName.contains("mash"))){
							if(mealName.contains("large"))
								typeTotals[8]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[9]+=quantities.get(sku);
						}
						else if(mealName.contains("veg")){
							if(mealName.contains("large"))
								typeTotals[10]+=quantities.get(sku);
							if(mealName.contains("small"))
								typeTotals[11]+=quantities.get(sku);
						}
					}
					
					
					
					String[] mealNameSplit = names.get(sku).split(" - ");
					sauceName = mealNameSplit[0];
					
					if (!(sku.contains("BALL")||(sku.contains("BAR"))))
					{
						if (sauceTotals.containsKey(sauceName)){ //if item already exists in hashMap
							sauceTotals.put(sauceName, sauceTotals.get(sauceName)+quantities.get(sku));					
						}
						else {
							sauceTotals.put(sauceName, quantities.get(sku));
							sauces.add(sauceName);
						}
					}
					
					
				//}
			}
			if(duplicates){
				totals.newLine();
				totals.write("ERROR! Duplicates found. File may be incorrect!");
				totals.newLine();
			}
			
					
			if (!sauceTotals.isEmpty()){
				//Writing the totals for each meal type
				totals.newLine();
				totals.newLine();
				totals.write("TYPE TOTALS"+","+"LARGE"+","+"SMALL");
				totals.newLine();
				totals.write("Steak + White Rice"+","+typeTotals[0]+","+typeTotals[1]);
				totals.newLine();
				totals.write("Steak + Sweet Potato"+","+typeTotals[2]+","+typeTotals[3]);
				totals.newLine();
				totals.write("Steak + Vege"+","+typeTotals[4]+","+typeTotals[5]);
				if(typeTotals[12] !=0 && typeTotals[13] !=0){
					totals.newLine();
					totals.write("Steak + Brown Rice & Vege"+","+typeTotals[12]+","+typeTotals[13]);
				}
				if(typeTotals[16] !=0 && typeTotals[17] !=0){
					totals.newLine();
					totals.write("Steak + Sweet Potato & Vege"+","+typeTotals[16]+","+typeTotals[17]);
				}
				totals.newLine();
				totals.write("Chicken + White Rice"+","+typeTotals[6]+","+typeTotals[7]);
				totals.newLine();
				totals.write("Chicken + Sweet Potato"+","+typeTotals[8]+","+typeTotals[9]);
				totals.newLine();
				totals.write("Chicken + Vege"+","+typeTotals[10]+","+typeTotals[11]);
				if(typeTotals[14] !=0 && typeTotals[15] !=0){
					totals.newLine();
					totals.write("Chicken + Brown Rice & Vege"+","+typeTotals[14]+","+typeTotals[15]);
				}
				if(typeTotals[18] !=0 && typeTotals[19] !=0){
					totals.newLine();
					totals.write("Chicken + Sweet Potato & Vege"+","+typeTotals[18]+","+typeTotals[19]);
				}

				
							
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
			totals.write("Large Meals"+","+larges+","+String.format("%1$,.2f", larges*100.0/total)+" %");
			totals.newLine();
			totals.write("Small Meals"+","+smalls+","+String.format("%1$,.2f", smalls*100.0/total)+" %");
			totals.newLine();
			totals.write("Snacks"+","+snacks+","+String.format("%1$,.2f", snacks*100.0/total)+" %");
				
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
	public static void CalcPrintIngredients(HashMap<String, Integer> quantities, HashMap<String, String> names){
		
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

		/*
		 * Totalling Ingredient Quantities
		 * This is accomplished by searching the menu item name for keywords like large, chicken, rice, etc
		 * i.e 'Pepperberry Steak - Large/Rice' would add 200g to the beef and rice quantities
		 */
		for(String sku : quantities.keySet()){
			String tempName = names.get(sku).toLowerCase();

			if(tempName.contains("supabarn")){
				ingredients.get(chicken).addQuantity(quantities.get(sku), 100);
			}
			if(tempName.contains("large")){
				if(tempName.contains("chicken"))
					ingredients.get(chicken).addQuantity(quantities.get(sku), 150);
				if(tempName.contains("steak"))
					ingredients.get(steak).addQuantity(quantities.get(sku), 150);
				if(tempName.contains("meatballs")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 150);
					ingredients.get(wholemealPenne).addQuantity(quantities.get(sku), 350);
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
			if(tempName.contains("small")){
				if(tempName.contains("chicken"))
					ingredients.get(chicken).addQuantity(quantities.get(sku), 100);
				if(tempName.contains("steak"))
					ingredients.get(steak).addQuantity(quantities.get(sku), 100);
				if(tempName.contains("meatballs")){
					ingredients.get(mince).addQuantity(quantities.get(sku), 100);
					ingredients.get(wholemealPenne).addQuantity(quantities.get(sku), 250);
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
	public static void PrintShipping(HashMap<String, ArrayList<OrderItem>> ordersByShippingMethod, ArrayList <String> old){

		try
		{
			// creating a BufferedWriter instance with FileWriter
			// the flag set to 'true' tells it to append a file if file exists. 'false' creates/recreates the file
			BufferedWriter shipping = new BufferedWriter(new FileWriter("_shipping.csv", false));
			BufferedWriter notes = new BufferedWriter(new FileWriter("_delivery_notes.csv", false));
			BufferedWriter deliveries = new BufferedWriter(new FileWriter("_deliveries.csv", false));
			
			shipping.write("SHIPPING METHODS: " + ordersByShippingMethod.keySet().size());
			shipping.newLine();	shipping.newLine();	shipping.newLine();	shipping.newLine();	shipping.newLine();
			
			notes.write("NAME,ADDRESS,PHONE");
			notes.newLine();
			notes.write("EMAIL,NOTE,METHOD");
			notes.newLine();notes.newLine();notes.newLine();
			
			deliveries.write("Order ID"+","+"Shipping Name"+","+"Shipping Street"+","+"Shipping City"+","+"Postcode"+","+"Mobile"+","+"Notes");
			deliveries.newLine();


			//Looping through all shipping methods and within that, looping through all orders while printing
			for(String shippingMethod : ordersByShippingMethod.keySet()){
				shipping.write("     METHOD,     NAME,     ADDRESS,     NOTES,     ORDER ID");
				shipping.newLine();
				for(OrderItem order : ordersByShippingMethod.get(shippingMethod)){
					String shippingString = order.getShippingMethod() + "," + order.getShippingName() + "," + order.getShippingAddress() + ",";
					if(old.contains(order.getOrderID()))
						shippingString = shippingString + "**CONTAINS OLD** "+order.getNotes()+","+order.getOrderID();
					else
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
				}
				
				shipping.write("     TOTAL: " + ordersByShippingMethod.get(shippingMethod).size());
				shipping.newLine();	shipping.newLine();	shipping.newLine();
			}

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
