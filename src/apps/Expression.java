package apps;

import java.io.*;
import java.util.*;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;

	/**
	 * Scalar symbols in the expression
	 */
	ArrayList<ScalarSymbol> scalars;

	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;

	/**
	 * String containing all delimiters (characters other than variables and
	 * constants), to be used with StringTokenizer
	 */
	public static final String delims = " \t*+-/()[]";

	/**
	 * Initializes this Expression object with an input expression. Sets all
	 * other fields to null.
	 * 
	 * @param expr
	 *            Expression
	 */
	public Expression(String expr) {
		this.expr = expr;
	}

	/**
	 * Populates the scalars and arrays lists with symbols for scalar and array
	 * variables in the expression. For every variable, a SINGLE symbol is
	 * created and stored, even if it appears more than once in the expression.
	 * At this time, values for all variables are set to zero - they will be
	 * loaded from a file in the loadSymbolValues method.
	 */
	public void buildSymbols() {
		expr = expr.replaceAll("\\s+", "");
		scalars = new ArrayList<ScalarSymbol>();
		arrays = new ArrayList<ArraySymbol>();

		StringTokenizer st = new StringTokenizer(expr, delims, true);
		// Tokenize the String expr

		// the first token

		// the second token

		String symbol = st.nextToken();
		String Asymbol = "0";

		String number = "0123456789";
		while (st.hasMoreTokens()) {
			if (st.hasMoreTokens()) {
				Asymbol = st.nextToken();
			}

			if (delims.indexOf(symbol) == -1 && number.indexOf(symbol.charAt(0)) == -1) { // symbol
																							// is
																							// a
																							// variable
				if (Asymbol.equals("[")) {

					ArraySymbol item = new ArraySymbol(symbol);
					// the token before [ must be an array

					if (!arrays.contains(item)) {
						// check if item already in the list
						arrays.add(item);

					}
				} else {
					ScalarSymbol item2 = new ScalarSymbol(symbol);

					// jump after Asymbol because Asymbol is not variable
					if (!scalars.contains(item2)) {
						scalars.add(item2);
					}
				}
				symbol = Asymbol;

				continue;
			}

			else {// symbol is operator or integer
				symbol = Asymbol;
				continue;

			}

		}
		if (delims.indexOf(symbol) == -1 && number.indexOf(symbol.charAt(0)) == -1) { // symbol
																						// is
																						// a
																						// variable

			ScalarSymbol item2 = new ScalarSymbol(symbol);

			// jump after Asymbol because Asymbol is not variable
			if (!scalars.contains(item2)) {
				scalars.add(item2);
			}
		}

		/** COMPLETE THIS METHOD **/
	}

	/**
	 * Loads values for symbols in the expression
	 * 
	 * @param sc
	 *            Scanner for values input
	 * @throws IOException
	 *             If there is a problem with the input
	 */
	public void loadSymbolValues(Scanner sc) throws IOException {
		while (sc.hasNextLine()) {
			StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
			int numTokens = st.countTokens();
			String sym = st.nextToken();
			ScalarSymbol ssymbol = new ScalarSymbol(sym);
			ArraySymbol asymbol = new ArraySymbol(sym);
			int ssi = scalars.indexOf(ssymbol);
			int asi = arrays.indexOf(asymbol);
			if (ssi == -1 && asi == -1) {
				continue;
			}
			int num = Integer.parseInt(st.nextToken());
			if (numTokens == 2) { // scalar symbol
				scalars.get(ssi).value = num;
			} else { // array symbol
				asymbol = arrays.get(asi);
				asymbol.values = new int[num];
				// following are (index,val) pairs
				while (st.hasMoreTokens()) {
					String tok = st.nextToken();
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					asymbol.values[index] = val;
				}
			}
		}
	}

	/**
	 * Evaluates the expression, using RECURSION to evaluate subexpressions and
	 * to evaluate array subscript expressions.
	 * 
	 * @return Result of evaluation
	 */
	public float evaluate() {
		String delims2 = "()[]";
		expr = expr.replaceAll("\\s+", "");
		String check;
		Expression subExpr = new Expression(" ");
		String subExpression;
		String Bracket = "[]";
		StringTokenizer formular = new StringTokenizer(expr, delims, true);
		int count = 0;
		int count2 = 0;
		int index = 0;
		Stack<String> Roperator = new Stack<String>();// store reversed operator
		Stack<Float> Rnum = new Stack<Float>();// store reversed value
		while (formular.hasMoreTokens()) {
			String ptr = formular.nextToken();
			ScalarSymbol ss = new ScalarSymbol(ptr);
			ArraySymbol as = new ArraySymbol(ptr);
			if (delims.indexOf(ptr) != -1) {
				if (Bracket.indexOf(ptr) == -1) {

					Roperator.push(ptr);
				}

			}

			else {
				try {
					float value = Float.parseFloat(ptr);
					Rnum.push(value);

				} catch (NumberFormatException e) {
					if (scalars.contains(ss)) {
						Rnum.push((float) scalars.get(scalars.indexOf(ss)).value);
						// refer its' value from scalar list
					} else if (arrays.contains(as)) {
						count++;
						for (index = expr.indexOf("["); count != 0; index++) {
							if (expr.charAt(index) == '[') {
								if (index != expr.indexOf("[")) {
									count++;
								}

							}
							if (expr.charAt(index) == ']') {
								count--;
							}
							if (delims.indexOf(expr.charAt(index)) != -1 && delims2.indexOf(expr.charAt(index)) == -1) {
								check = formular.nextToken();
								check = formular.nextToken();
							}
							if (delims2.indexOf(expr.charAt(index)) != -1) {
								check = formular.nextToken();
							}
						}
						check = formular.nextToken();
						subExpression = expr.substring(expr.indexOf("[") + 1, index - 1);
						subExpr = new Expression(subExpression);
						subExpr.scalars = this.scalars;
						subExpr.arrays = this.arrays;
						Rnum.push((float) arrays.get(arrays.indexOf(as)).values[(int) subExpr.evaluate()]);
					}

					// refer its' value from array list

				}
			}
		}

		Stack<String> Operator = Reverse(Roperator);
		Stack<Float> Num = Reverse(Rnum);
		return Operation(Num, Operator);
	}

	private <T> Stack Reverse(Stack<T> R) {// reverse the order in the stack
		if (R.size() <= 1) {
			return R;
		} else {
			Stack<T> reverse = new Stack<T>();
			while (!R.isEmpty()) {
				reverse.push(R.pop());
			}
			return reverse;
		}
	}

	private float Operation(Stack<Float> num, Stack<String> operator) {
		Stack<String> parenMatch = new Stack<String>();

		float Tnumber = 0;
		if (num.isEmpty()) {
			return 0;
		} else if (operator.isEmpty() && !num.isEmpty()) {
			return num.pop();
		} else {
			String Operator1 = operator.pop();

			float Number1 = num.pop();

			if (Operator1.equals("+")) {

				num.push(Number1 + Operation(num, operator));
				return Operation(num, operator);
			} else if (Operator1.equals("-")) {
				 
				return Operation(num, operator);
			} else if (Operator1.equals("*")) {
				if (!operator.isEmpty()) {
					if (operator.peek().equals("(")) {
						num.push(Number1 * Operation(num, operator));
						return Operation(num, operator);
					} else {
						Tnumber = num.pop();
						Number1 = Number1 * Tnumber;
						num.push(Number1);
						return Operation(num, operator);

					}
				} else {
					Tnumber = num.pop();
					Number1 = Number1 * Tnumber;
					num.push(Number1);
					return Operation(num, operator);
				}
			}

			else if (Operator1.equals("/")) {
				if (!operator.isEmpty()) {
					if (operator.peek().equals("(")) {
						num.push(Number1 / Operation(num, operator));
						return Operation(num, operator);
					} else {
						Tnumber = num.pop();
						Number1 = Number1 / Tnumber;
						num.push(Number1);
						return Operation(num, operator);

					}
				} else {
					Tnumber = num.pop();
					Number1 = Number1 / Tnumber;
					num.push(Number1);
					return Operation(num, operator);
				}
			}

			else {// if(Operator1.equals("("))
				int count = 0;
				if (!operator.isEmpty()) {

					count++;
				} // pop the first "("
				num.push(Number1);// restore a number
				Stack<Float> Tnum = new Stack<Float>();// store the value inside
														// of the paren
				Stack<String> Toperator = new Stack<String>();// store the
																// operator
																// inside of the
																// paren
				if (!operator.isEmpty()) {
					Tnum.push(num.pop());
					while (count != 0) {

						Toperator.push(operator.pop());
						if (Toperator.peek().equals("(")) {
							count++;
						} else if (Toperator.peek().equals(")")) {
							count--;
						} else {// Toperator is a real operator
							Tnum.push(num.pop());
						}
						if (operator.isEmpty()) {
							break;
						}

					}
					if (Toperator.peek().equals(")")) {
						Toperator.pop();
					}
				}

				// parenMatch.push("(");
				Tnum = Reverse(Tnum);
				Toperator = Reverse(Toperator);
				num.push(Operation(Tnum, Toperator));
				return Operation(num, operator);
			}

		}
	}

	/**
	 * Utility method, prints the symbols in the scalars list
	 */
	public void printScalars() {
		for (ScalarSymbol ss : scalars) {
			System.out.println(ss);
		}
	}

	/**
	 * Utility method, prints the symbols in the arrays list
	 */
	public void printArrays() {
		for (ArraySymbol as : arrays) {
			System.out.println(as);
		}
	}


}
