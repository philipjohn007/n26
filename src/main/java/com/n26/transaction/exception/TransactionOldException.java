package com.n26.transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NO_CONTENT)
public class TransactionOldException extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
