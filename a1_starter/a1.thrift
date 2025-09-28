exception IllegalArgument {
  1: string message;
}

service MiningPoolService {
 i64 mineBlock (1: i32 version, 2: binary prevBlockHash, 3: binary merkleRootHash, 4: i64 time, 5: i64 target) throws (1: IllegalArgument e);
 void cancel();
}
