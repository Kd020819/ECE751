import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.layered.TFramedTransport;


public class FEServer {
    static Logger log;

    public static void main(String [] args) throws Exception {
	if (args.length != 1) {
	    System.err.println("Usage: java FEServer port");
	    System.exit(-1);
	}

	// initialize log4j
	BasicConfigurator.configure();
	log = Logger.getLogger(FEServer.class.getName());

	int port = Integer.parseInt(args[0]);
	log.info("Launching server on port " + port);

	// launch Thrift server
	MiningPoolService.Processor processor = new MiningPoolService.Processor<MiningPoolService.Iface>(new MiningPoolServiceHandler());
	TServerSocket socket = new TServerSocket(port);
	TSimpleServer.Args sargs = new TSimpleServer.Args(socket);
	sargs.protocolFactory(new TBinaryProtocol.Factory());
	sargs.transportFactory(new TFramedTransport.Factory());
	sargs.processorFactory(new TProcessorFactory(processor));
	TSimpleServer server = new TSimpleServer(sargs);
	server.serve();
    }
}
