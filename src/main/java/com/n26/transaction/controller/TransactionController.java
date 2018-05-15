package com.n26.transaction.controller;

import java.util.*;
import java.util.function.Predicate;

import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import com.n26.transaction.exception.*;
import com.n26.transaction.model.*;

@RestController
@RequestMapping("/api")
public class TransactionController {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionController.class);

	List<Transaction> transactions = new ArrayList<>();
	Statistics statistics = new Statistics();

	Predicate<Transaction> transactionsPredicate = transaction -> 
		System.currentTimeMillis() - transaction.getTimestamp() > 60000;

	@GetMapping("/statistics")
	public @ResponseBody ResponseEntity<Statistics> getStatistics() {
		return new ResponseEntity<>(statistics, HttpStatus.OK);
	}

	@PostMapping("/transactions")
	public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
		if (Objects.isNull(transaction.getTimestamp())) {
			throw new EmptyTimestampException();
		}

		if (System.currentTimeMillis() - transaction.getTimestamp() > 60000) {
			throw new TransactionOldException();
		}

		transactions.add(transaction);
		updateStatistics();

		return new ResponseEntity<Transaction>(HttpStatus.CREATED);
	}

	// Check for and remove old transactions every one second
	@Scheduled(fixedRate = 1000)
    public void removeOldTransactions() {
		boolean isRemoved = this.transactions.removeIf(transactionsPredicate);
		if (isRemoved) {
			if (this.transactions.isEmpty()) {
				statistics = new Statistics();
			} else {
				updateStatistics();
			}
		}
    }

	private void updateStatistics() {
		LOG.info("Updating statistics");
		statistics.setCount(Long.valueOf(this.transactions.size()));
		statistics.setMax(Collections.max(transactions, Comparator.comparing(Transaction::getAmount)).getAmount());
		statistics.setMin(Collections.min(transactions, Comparator.comparing(Transaction::getAmount)).getAmount());
		statistics.setAvg(this.transactions.stream().mapToDouble(t -> t.getAmount()).average().orElse(0));
		statistics.setSum(this.transactions.stream().mapToDouble(t -> t.getAmount()).sum());
	}
}
