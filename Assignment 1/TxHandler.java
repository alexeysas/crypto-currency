import java.util.HashSet;
import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
  
    private UTXOPool innerPool;
      
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
      
       innerPool = new UTXOPool(utxoPool);
                  
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
      
         HashSet<UTXO> claimedSet = new HashSet<UTXO>();
         double inputSum = 0;
         
         int n = tx.numInputs();
         
         for (int i = 0; i < n; i++) {
           
           Transaction.Input input = tx.getInput(i);
           UTXO utx = new UTXO(input.prevTxHash, input.outputIndex);
           
           // Check if transaction is in uspent pool
           if (!innerPool.contains(utx)) {
             return false;
           }
              
           // check if signature is valid for input
           Transaction.Output output =  innerPool.getTxOutput(utx);
               
           byte[] bytesToSign = tx.getRawDataToSign(i);
           
           boolean signatureValid = Crypto.verifySignature(output.address, bytesToSign, input.signature);
           
           if (!signatureValid) {
             return false;
           }
               
           // check if there double-spend attack 
           boolean isClaimed = claimedSet.contains(utx); 
           
           if (!isClaimed) {
             claimedSet.add(utx);
           }
           else {
              return false;
           }
           
           inputSum += output.value;
        }

       int m = tx.numOutputs();
       double outputSum = 0;
        
       for (int i = 0; i <m ; i++) {
         Transaction.Output output = tx.getOutput(i);
          
         // Check if we have negtive values
         if (output.value < 0) {
            return false;  
         }
         
          outputSum += output.value;
       }       
       
       // check if we try to spend more than we have
       if (outputSum > inputSum) {
         return false;
       }
       return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
      
     // public void addUTXO(UTXO utxo, Transaction.Output txOut) {
     //   H.put(utxo, txOut);
   // }

    /** Removes the UTXO {@code utxo} from the pool */
   // public void removeUTXO(UTXO utxo) {
   //     H.remove(utxo);
   // }
      
      
      int numberTransactions = possibleTxs.length;
      ArrayList<Transaction> remaining = new ArrayList<Transaction>();
      ArrayList<Transaction> newRemaining = new ArrayList<Transaction>();
      ArrayList<Transaction> accepted = new ArrayList<Transaction>();
      for (Transaction t : possibleTxs) {
        remaining.add(t);
      }           
     
      boolean added = false;
     
      do
      {
        added = false;
     
        for (Transaction t : remaining) {
          if (isValidTx(t)) {
            
            // delete spent transaction outputs
            int n = t.numInputs();
            
            for (int i = 0; i < n; i++) {
              Transaction.Input input = t.getInput(i);
              UTXO utx = new UTXO(input.prevTxHash, input.outputIndex);
              innerPool.removeUTXO(utx);
            }
            
            int m = t.numOutputs();
            
            for (int i = 0; i <m ; i++) {
              Transaction.Output output = t.getOutput(i);
              UTXO utx = new UTXO(t.getHash(), i);
              innerPool.addUTXO(utx, output);
            }
            accepted.add(t);
            added = true;
          }
          else {
            newRemaining.add(t);
          }
        }
        remaining = newRemaining; 
        newRemaining = new ArrayList<Transaction>();
     } 
     while(numberTransactions > 0 && added );
            
      return accepted.toArray(new Transaction[accepted.size()]);
    }

}
