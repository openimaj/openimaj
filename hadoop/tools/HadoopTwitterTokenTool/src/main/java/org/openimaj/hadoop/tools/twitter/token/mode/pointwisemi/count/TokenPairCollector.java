package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

/**
 * Assumes TokenPairCount instances will be added in order.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
class TokenPairCollector{
	private TokenPairCount currentToken;
	private String currentString;

	public TokenPairCollector(){
		this.currentToken = null;
		this.currentString = null;
	}
	
	public TokenPairCount add(TokenPairCount count){
		if(this.currentToken == null){
			this.currentToken = count;
			this.currentString = count.identifier();
//			System.out.println("Starting with: " + currentString);
			return null;
		}
		
		String nextid = count.identifier();
		if(this.currentString.equals(nextid)){
			this.currentToken.add(count);
			return null;
		}
		else
		{
			TokenPairCount toRet = this.currentToken;
			this.currentToken = count;
			this.currentString = count.identifier();
//			System.out.println("Switching to: " + currentString);
			return toRet;
		}
		
	}

	public boolean isCurrentPair() {
		return currentToken != null && !currentToken.isSingle;
	}

	public TokenPairCount getCurrent() {
		return this.currentToken;
	}
	
}