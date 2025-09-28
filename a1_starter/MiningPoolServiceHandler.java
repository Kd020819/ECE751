import java.util.ArrayList;
import java.util.List;

public class MiningPoolServiceHandler implements MiningPoolService.Iface {
    public long mineBlock(int version, java.nio.ByteBuffer prevBlockHash, java.nio.ByteBuffer merkleRootHash, long time, long target) throws IllegalArgument, org.apache.thrift.TException {
	return 43;
    }

    public void cancel() {
    }
}
