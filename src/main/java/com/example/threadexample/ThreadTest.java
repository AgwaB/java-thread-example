package com.example.threadexample;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;

@Component
public class ThreadTest implements CommandLineRunner {

  @Autowired
  private Web3j web3j;

  @Override
  public void run(String... args) throws Exception {
    long start = System.nanoTime();
    System.out.println("withCompletableFutureAndFixedThreadPool");
    this.withCompletableFutureAndFixedThreadPool();
    long duration = (System.nanoTime() - start) / 1_000_000;
    System.out.printf("execution time - %d millis\n", duration);

    start = System.nanoTime();
    System.out.println("withParallelStreamAndForkJoinPool");
    this.withParallelStreamAndForkJoinPool();
    duration = (System.nanoTime() - start) / 1_000_000;
    System.out.printf("execution time - %d millis\n", duration);

    start = System.nanoTime();
    System.out.println("withCompletableFutureAndForkJoinPool");
    this.withCompletableFutureAndForkJoinPool();
    duration = (System.nanoTime() - start) / 1_000_000;
    System.out.printf("execution time - %d millis\n", duration);
  }

  private void withCompletableFutureAndFixedThreadPool() {
    Executor executor = Executors.newFixedThreadPool(10);
    List<CompletableFuture<BigInteger>> futures = IntStream.range(0, 10)
        .mapToObj((index) -> CompletableFuture.supplyAsync(this::getLatestBlockNumber, executor))
        .collect(Collectors.toList());
    futures.stream()
        .map(CompletableFuture::join)
        .forEach(System.out::println);
  }

  private void withCompletableFutureAndForkJoinPool() {
    ForkJoinPool forkJoinPool = new ForkJoinPool(10);
    List<CompletableFuture<BigInteger>> futures = IntStream.range(0, 10)
        .mapToObj((index) -> CompletableFuture.supplyAsync(this::getLatestBlockNumber, forkJoinPool))
        .collect(Collectors.toList());
    futures.stream()
        .map(CompletableFuture::join)
        .forEach(System.out::println);
  }

  private void withParallelStreamAndForkJoinPool() {
    ForkJoinPool forkJoinPool = new ForkJoinPool(10);
    try {
      forkJoinPool.submit(() -> IntStream.range(0, 10)
      .forEach((index) -> this.getLatestBlockNumber())).get();
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private BigInteger getLatestBlockNumber() {
    try {
      return this.web3j.ethBlockNumber().send().getBlockNumber();
    } catch (Exception e) {
      System.out.println("fail to get latest block number");
      throw new RuntimeException();
    }
  }
}
