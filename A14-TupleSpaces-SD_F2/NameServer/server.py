import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

import grpc
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc

from NamingServerServiceImpl import NameServerServiceImpl
from concurrent import futures
from NamingServerState import ServerEntry, ServiceEntry, NamingServerState

# Define the port number on which the server will listen
PORT = 5001
    
if __name__ == '__main__':
    try:
        # Create a gRPC server with a thread pool executor to handle incoming requests
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=3))

        # Add the service implementation to the server
        pb2_grpc.add_NameServerServiceServicer_to_server(NameServerServiceImpl(), server)

        # Configure the server to listen on the specified port
        server.add_insecure_port(f'[::]:{PORT}')
        
        # Start the server
        server.start()
        print(f"Naming Server listening on port {PORT}")
        print("Press CTRL+C to terminate")

        # Keep the server running until it's manually stopped
        server.wait_for_termination()

    except KeyboardInterrupt:
        # Stop the server on keyboard interrupt (CTRL+C)
        print("NamingServer stopped")
        exit(0)
