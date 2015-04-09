	//Holds sample data types/vals
	public static ArrayList<Object> sampleVals;
	public static ArrayList<String> sampleTypes;
	public static ArrayList<ArrayList<Object>> valHolder; 
	public static AbstractMap<Integer, ArrayList<String>> typeHolder; // = new AbstractMap<String, Integer>();
	//public static EnumMap<Integer, ArrayList<String>> typeHolder;

	//TINYINT, SMALLINT, MEDIUMINT, INT and DECIMAL.
	//CHAR, VARCHAR
	//DATETIME
	static Pattern INT = Pattern.compile("[\\+\\-]?\\d+");
	static Pattern DECIMAL = Pattern.compile("[\\+\\-]?\\d+\\.\\d+(?:[eE][\\+\\-]?\\d+)?");
	static Pattern CHAR = Pattern.compile("[^0-9]");
	static Pattern DDMMYYYY = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)\\d\\d)");
	static Pattern MMDDYYYY = Pattern.compile("(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)");
	static Pattern MMDDYY = Pattern.compile("(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/(\\d\\d)");
	static Pattern HOUR24 = Pattern.compile("([01]?[0-9]|2[0-3]):[0-5][0-9]");
	static Pattern HOUR12 = Pattern.compile("(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)");
	static Pattern IPADDRESS = Pattern.compile("([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
	static Pattern EMAIL = Pattern.compile("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})");

	//Makes 2D arraylist of top 30 samples
	public static void getSample() throws IOException {

		File file = new File(path.getAbsolutePath());
		FileReader input = new FileReader(file); 
		CSVParser parser = new CSVParser(input);
		String[] values = parser.getLine(); 

		valHolder = new ArrayList<ArrayList<Object>>();
		int row = 0;
		while (parser.getLineNumber() < 15) {	
			sampleVals = new ArrayList<Object>();
			int col = 0;
			for (String val : values) {
				sampleVals.add(col, val);
				col++;
			}
			valHolder.add(row, sampleVals);
			row++;
			values = parser.getLine();
		}
		input.close();
		getDType();
	}

	public static void getDType() {
		//typeHolder = new AbstractMap<Integer, ArrayList<String>>();
		//typeHolder = new HashMap<String, ArrayList<String>>();
		for (ArrayList<Object> arr : valHolder) {
			int index = -1;
			for (Object testVal : arr) {
				String test = (String) testVal;
				//check if each already has the <column, datatype> pair
				if (CHAR.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("CHAR");
						typeHolder.put(index, sampleTypes); 
					}
				}
				else if (DECIMAL.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("DECIMAL");
						typeHolder.put(index, sampleTypes); 
					}
				}
				else if (INT.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("INT");
						typeHolder.put(index, sampleTypes); 
					}
				}
				else if (DDMMYYYY.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("DDMMYYYY");
						typeHolder.put(index, sampleTypes); 
					}
				}

				else if (MMDDYYYY.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("MMDDYYYY");
						typeHolder.put(index, sampleTypes); 
					}
				}
				else if (MMDDYY.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("MMDDYY");
						typeHolder.put(index, sampleTypes); 
					}
				}
				else if (HOUR24.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("HOUR24");
						typeHolder.put(index, sampleTypes); 
					}
				}
				else if (HOUR12.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("HOUR12");
						typeHolder.put(index, sampleTypes); 
					}
				}
				else if (IPADDRESS.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("IPADDRESS");
						typeHolder.put(index, sampleTypes);  
					}
				}
				else if (EMAIL.matcher(test).matches()) {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("EMAIL");
						typeHolder.put(index, sampleTypes); 
					}
				}

				else {
					if (typeHolder.containsKey(index)) { continue; }
					else {  //if not, then add it
						sampleTypes = new ArrayList<String>();
						sampleTypes.add("VARCHAR");
						typeHolder.put(index, sampleTypes); 
					}
				}
			}
		}
	}
}