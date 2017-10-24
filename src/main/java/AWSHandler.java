import java.io.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


public class AWSHandler {

    public void run () {
        try {
            List<String> properties = getPropertiesValues();
            InetAddress ip = InetAddress.getByName(properties.get(0));
            String usuario = properties.get(1);
            String senha = properties.get(2);
            int porta = Integer.parseInt(properties.get(3));
            String sourcebucket= "lab-ph2"; //nome do bucket
            String sourcekey = "Eiffel.jpg"; // nome do arquivo
            String diretorioRemoto = properties.get(6);
            File file = new File (properties.get(7));


            FTPTransfer(ip,usuario,senha,diretorioRemoto,porta,file);

            S3Object s3object = amazonDownload(sourcebucket,sourcekey);
            System.out.println("Content-Type: "  + s3object.getObjectMetadata().getContentType());
            displayTextInputStream(s3object.getObjectContent());

            amazonUpload(sourcebucket,sourcekey,file);
            amazonUpload(sourcebucket,sourcekey,file);



        }
        catch (IOException e){
            System.out.println("Erro "+ e.getMessage());

        }


    }

    public void  FTPTransfer(InetAddress ip, String usuario, String senha, String diretorioRemoto, int porta, File file) throws IOException {
        //exemplo em https://commons.apache.org/proper/commons-net/examples/ftp/FTPClientExample.java
        FTPClient ftp = new FTPClient();
        ftp.connect(ip,porta);
        ftp.login(usuario, senha);

        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.ASCII_FILE_TYPE);

        String caminhoArq = diretorioRemoto + file.getName();
        FileInputStream arqEnviar =

                new FileInputStream(diretorioRemoto);

        if (ftp.storeFile (caminhoArq, arqEnviar))

            System.out.println("Arquivo armazenado com sucesso!");
        else
            System.out.println ("Erro ao armazenar o arquivo.");

        ftp.logout();
        ftp.disconnect();

    }

    public S3Object amazonDownload(String sourcebucket, String sourcekey){
        AmazonS3 s3Client = new AmazonS3Client();
        S3Object object = null;

        try {

            System.out.println("Downloading an object");
            object = s3Client.getObject(new GetObjectRequest(sourcebucket, sourcekey));



        }
        catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return object;

    }

    public void amazonUpload(String sourcebucket, String sourcekey, File file){

        AmazonS3 s3 = new AmazonS3Client();

        try {

            System.out.println("Uploading a new object to S3 from a file\n");
            s3.putObject(new PutObjectRequest(sourcebucket, sourcekey, file));


        }
        catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }




    }

    public String lambdaHandler(S3Event event, Context context){
        LambdaLogger logger = context.getLogger();
        logger.log("received : " + event.toString());
        return event.name();
    }

    private File createSampleFile() throws IOException {
        File file = File.createTempFile("aws-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("01234567890112345678901234\n");
        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
        writer.write("01234567890112345678901234\n");
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        return file;
    }

    private void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }

    private  List<String> getPropertiesValues() throws IOException {
        InputStream inputStream=null;
        List<String> resultado = new ArrayList<String>();
            try {
                Properties prop = new Properties();
                String propFileName = "config.properties";

                inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

                if (inputStream != null) {
                    prop.load(inputStream);
                } else {
                    throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
                }

                // pegando os valores das propriedades e adicionando ao resultado
                String ip = prop.getProperty("IP");
                resultado.add(ip);
                String usuario = prop.getProperty("usuario");
                resultado.add(usuario);
                String senha = prop.getProperty("senha");
                resultado.add(senha);
                String porta = prop.getProperty("porta");
                resultado.add(porta);
                String sourcebucket = prop.getProperty("sourcebucket");
                resultado.add(sourcebucket);
                String sourcekey = prop.getProperty("sourcekey");
                resultado.add(sourcekey);
                String diretorioRemoto = prop.getProperty("diretorioRemoto");
                resultado.add(diretorioRemoto);
                String file = prop.getProperty("file");
                resultado.add(file);


            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }

             finally {
                inputStream.close();
            }
            return resultado;
    }



}