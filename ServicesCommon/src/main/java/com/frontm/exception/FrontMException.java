package com.frontm.exception;

@SuppressWarnings("serial")
public class FrontMException extends Exception {
	private FRONTM_ERROR_CODE errorCode;

	public FrontMException(String message) {
		super(message);
		this.errorCode = FRONTM_ERROR_CODE.UNMAPPED_ERROR;
	}
	
	public FrontMException(FRONTM_ERROR_CODE e) {
		this.errorCode = e;
	}
	
	public FRONTM_ERROR_CODE getErrorCode() {
		return errorCode;
	}
	
	public static enum FRONTM_ERROR_CODE {
		UNAVAILABLE_INSTANCE_ID(1, "Instance Id not found"),
		UNAVAILABLE_COMMAND(2, "Command not found"),
		MISSING_INPUT(3, "Required input missing"),
		UNMAPPED_ERROR(99, "Unmapped error");
		
		private int errorNumber;
		private String errorMessage;
		FRONTM_ERROR_CODE(int errorNum, String errorMessage) {
			this.errorNumber = errorNum;
			this.errorMessage = errorMessage;
		}
		public int getErrorNumber() {
			return errorNumber;
		}
		public String getErrorMessage() {
			return errorMessage;
		}
	}
}
