package com.n26.transaction.controller;

import java.util.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.n26.transaction.exception.*;
import com.n26.transaction.model.Transaction;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
	List<Transaction> transactions = new ArrayList<>();
	
	@GetMapping()
	public ResponseEntity<List<Transaction>> getTransactions() {
		return new ResponseEntity<>(transactions, HttpStatus.OK);
	}

	@PostMapping()
	public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
		if (Objects.isNull(transaction.getTimestamp())) {
			throw new EmptyTimestampException();
		}

		if (System.currentTimeMillis() - transaction.getTimestamp() > 60000) {
			throw new TransactionOldException();
		}

		transactions.add(transaction);

		return new ResponseEntity<Transaction>(HttpStatus.CREATED);
	}

}
