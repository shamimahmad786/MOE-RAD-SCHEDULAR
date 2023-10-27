package MOERADSCHEDULAR.CTRL;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import MOERADSCHEDULAR.UTILITY.NativeRepository;
import MOERADSCHEDULAR.UTILITY.QueryResult;

@Service
@EnableScheduling
public class OtpSchedular {
	
	@Autowired
	NativeRepository nativeRepository;
	
	  private RestTemplate restTemplate = new RestTemplate();

	    public void RestMailService(RestTemplateBuilder restTemplateBuilder) {
	        this.restTemplate = restTemplateBuilder.build();
	    }

	
	 @Scheduled(fixedDelay = 1000)
	public void otpMailSchedule() throws InterruptedException {
		 
		 Map<String,Object> data;
		 QueryResult queryResult= getUserOtp();
		 
		 HttpHeaders headers = new HttpHeaders();
        
		 try {
		 for(Map<String,Object> dataObj : queryResult.getRowValue()) {
			 
			 
			 
		 String smsJSON="{ \"mobile\":'"+dataObj.get("mobile")+"', \"otpId\":\"OTP-3\", \"applicationId\":1, \"dynamicData\":['KVS','"+dataObj.get("otp")+"','KVS'] }".replaceAll("'", "\"");
         smsJSON=smsJSON.replaceAll("'", "\"");
         
         System.out.println("smsJSON---->"+smsJSON);
         
             try {           
             	HttpEntity<String> request = new HttpEntity<String>(smsJSON,headers);
     	        String url = "http://10.247.141.227:8080/ME-RAD-MESSAGE/api/sendOTP";
     	        this.restTemplate.exchange(url, HttpMethod.POST, request,Map.class,1);	
     	        updateOtp(String.valueOf(dataObj.get("mobile")),"S");
             }catch(Exception ex) {        
            	  updateOtp(String.valueOf(dataObj.get("mobile")),"E");
             	ex.printStackTrace();
             }
		 }
		 }catch(Exception ex) {
			 ex.printStackTrace();
		 }   
		 
 }
	 
	 
	 @Scheduled(fixedDelay = 1000)
		public void deleteOtp() throws InterruptedException {
//		 nativeRepository.updateQueries("update oauth_user_otp set status='E' where EXTRACT(EPOCH FROM (now() - createdby)) >120 ");
		 nativeRepository.insertQueries("insert into oauth_user_otp_history (select * from oauth_user_otp where EXTRACT(EPOCH FROM (now() - createdby)) >120)");
		 nativeRepository.updateQueries("delete from oauth_user_otp where  EXTRACT(EPOCH FROM (now() - createdby)) >120"); 
	 }
	 
	 
	 
	 public QueryResult getUserOtp() {
		 return nativeRepository.executeQueries("select * from oauth_user_otp where status is null or status=''");
	 }
	 
	 
	 public void updateOtp(String mobile,String status) {
		 nativeRepository.updateQueries("update oauth_user_otp set status='"+status+"' where mobile='"+mobile+"'");
//		 nativeRepository.insertQueries("insert into oauth_user_otp_history (select * from oauth_user_otp where mobile='"+mobile+"')");
//		 nativeRepository.updateQueries("delete from oauth_user_otp where mobile='"+mobile+"'");
	 }
	 
	 
	
}
