package es.um.sisdist.backend.grpc.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PingResponse;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase 
{
	private Logger logger;
	
	public GrpcServiceImpl(Logger logger) 
	{
		super();
		this.logger = logger;
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) 
	{
		logger.info("Recived PING request, value = " + request.getV());
		responseObserver.onNext(PingResponse.newBuilder().setV(request.getV()).build());
		responseObserver.onCompleted();
	}

	public void prompt(PromptRequest request, StreamObserver<PromptResponse> responseObserver) 
	{
		logger.info("Received Prompt request, prompt = " + request.getPrompt() + 
		", token = " + request.getToken());
		HttpClient client = HttpClient.newHttpClient();

		String json = "{\"prompt\": \"" + request.getPrompt() + "\"}";
		
	
		HttpResponse<String> httpResponse;
		try {
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(new URI("http://ssdd-llamachat:5020/prompt"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.build();

		logger.info("url: " + httpRequest.uri());
		logger.info("data: " + json);
		httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		logger.info(httpResponse.toString());
		if (httpResponse.statusCode() == 202) {
            String location = "http://ssdd-llamachat:5020" + httpResponse.headers().firstValue("Location").get();
			new Thread(() -> {
                try {
                    HttpRequest llamaRequest = HttpRequest.newBuilder()
                            .uri(new URI(location))
                            .GET()
                            .build();
                    
                    while(true){
                        // Enviar la solicitud GET y manejar la respuesta
						HttpResponse<String> getResponse = client.send(llamaRequest, HttpResponse.BodyHandlers.ofString());
                        if(getResponse.statusCode() == 200){
                            JsonObject jsonObject = JsonParser.parseString(getResponse.body()).getAsJsonObject();
                            String answer = jsonObject.get("answer").getAsString();
                            logger.info("Answer: " + answer);
                            
                            PromptResponse response = PromptResponse.newBuilder().setAnswer(answer).build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                            break;
                        }
                        Thread.sleep(5000);
                        
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

			}).start();
		}

		} catch (IOException | InterruptedException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
		


/*
	@Override
	public void storeImage(ImageData request, StreamObserver<Empty> responseObserver)
    {
		logger.info("Add image " + request.getId());
    	imageMap.put(request.getId(),request);
    	responseObserver.onNext(Empty.newBuilder().build());
    	responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<ImageData> storeImages(StreamObserver<Empty> responseObserver) 
	{
		// La respuesta, sólo un objeto Empty
		responseObserver.onNext(Empty.newBuilder().build());

		// Se retorna un objeto que, al ser llamado en onNext() con cada
		// elemento enviado por el cliente, reacciona correctamente
		return new StreamObserver<ImageData>() {
			@Override
			public void onCompleted() {
				// Terminar la respuesta.
				responseObserver.onCompleted();
			}
			@Override
			public void onError(Throwable arg0) {
			}
			@Override
			public void onNext(ImageData imagedata) 
			{
				logger.info("Add image (multiple) " + imagedata.getId());
		    	imageMap.put(imagedata.getId(), imagedata);	
			}
		};
	}

	@Override
	public void obtainImage(ImageSpec request, StreamObserver<ImageData> responseObserver) {
		// TODO Auto-generated method stub
		super.obtainImage(request, responseObserver);
	}

	@Override
	public StreamObserver<ImageSpec> obtainCollage(StreamObserver<ImageData> responseObserver) {
		// TODO Auto-generated method stub
		return super.obtainCollage(responseObserver);
	}
	*/
}