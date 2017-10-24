import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


import java.io.InputStream;

public class AWSLambda implements RequestHandler<String, String> {

    public String handleRequest(String input, Context context) {
        context.getLogger().log("Input: " + input);


        // A ideia é trocar esta parte parece receber um S3Event ao invés de input puro
        return "Este é o input "+input+ " dentro deste contexto "+context.toString();
    }

    public Object handleRequest(S3Event input, Context context){
        AmazonS3Client s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());

        for (S3EventNotification.S3EventNotificationRecord record : input.getRecords()) {

            String s3Key = record.getS3().getObject().getKey();
            String s3Bucket = record.getS3().getBucket().getName();
            context.getLogger().log("found id: " + s3Bucket+" "+s3Key);
            // recebe objeto do S3
            S3Object object = s3Client.getObject(new GetObjectRequest(s3Bucket, s3Key));
            InputStream objectData = object.getObjectContent();
            // fazer algo com o objeto
        }

        return "sucess";
    }
}
