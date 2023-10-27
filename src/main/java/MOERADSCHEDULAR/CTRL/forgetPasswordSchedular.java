package MOERADSCHEDULAR.CTRL;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import MOERADSCHEDULAR.UTILITY.MailBean;
import MOERADSCHEDULAR.UTILITY.NativeRepository;
import MOERADSCHEDULAR.UTILITY.QueryResult;

@Service
@EnableScheduling
public class forgetPasswordSchedular {

	@Autowired
	NativeRepository nativeRepository;

	private RestTemplate restTemplate = new RestTemplate();

	public void RestMailService(RestTemplateBuilder restTemplateBuilder) {
		this.restTemplate = restTemplateBuilder.build();
	}

	@Scheduled(fixedDelay = 1000)
	public void forgetMail() throws InterruptedException {
		HttpHeaders headers = new HttpHeaders();
		QueryResult usrFrogetObj = getUserDetails();
		for (Map<String, Object> dataObj : usrFrogetObj.getRowValue()) {
			MailBean obj = new MailBean();
			obj.setApplicationName("Kvs Teacher");
			obj.setApplicationId("1");
			obj.setEmailTemplateId("MSG-5836");
			obj.setEmailTo(String.valueOf(dataObj.get("mail")));
			obj.setSubject("Teacher Module Credential");
			obj.setSignature("Dear " + dataObj.get("username"));
			
//			https://demopgi.udiseplus.gov.in/school_1/#/restPassword?sessionId=123
//			http://localhost:4200/#/restPassword
			obj.setContent("A passworrd reset for your account was requested.<br> Please click the link below to change password. <br> <a href=https://demopgi.udiseplus.gov.in/school_1/#/restPassword?sessionId="+dataObj.get("sessionid")+">Reset</a> <br> Note that this link is valid for 10 min. After the time limit has expired,you will have to resubmit the request for a possword reset- Team NIC -Ministry of Education, Government of India");
			obj.setClosing("KVS Headquarter");
			obj.setMobile("");
			obj.setUserid(String.valueOf(dataObj.get("username")));
			obj.setName(String.valueOf(dataObj.get("username")));
			obj.setAttachmentYn(null);
			obj.setAttachmentPath(null);

			try {
				String requestJson = "{ \"applicationName\":\"Kvs Teacher\",\"attachmentYn\":\"0\" ,\"attachmentPath\":\"\",\"applicationId\":\"1\", \"emailTemplateId\": \"MSG-5836\", \"emailTo\": '"
						+ obj.getEmailTo()
						+ "', \"emailCc\": \"shamim.ahmad586@gmail.com\", \"subject\": \"Forget Password \", \"signature\": '"
						+ obj.getSignature() + "', \"content\": '" + obj.getContent() + "', \"closing\":'"
						+ obj.getClosing() + "' }".replaceAll("'", "\"");
				requestJson=requestJson.replaceAll("'", "\"");
				
				System.out.println("Only dynamic content--->"+obj.getContent());
				System.out.println("Request JSON--->"+requestJson);

				try {
					HttpEntity<String> request = new HttpEntity<String>(requestJson, headers);
//					String url = "http://localhost:8686/api/sendMessage";
					String url = "http://10.247.141.227:8080/ME-RAD-MESSAGE/api/sendMessage";
					this.restTemplate.exchange(url, HttpMethod.POST, request, Map.class, 1);
					updateForgetPasswordStatus(obj.getEmailTo(),"S");
				} catch (Exception ex) {
					updateForgetPasswordStatus(obj.getEmailTo(),"E");
					ex.printStackTrace();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	public QueryResult getUserDetails() {
		return nativeRepository.executeQueries("select * from public.oauth_forget_password where status is null or status='' limit 5");
	}
	
	public void updateForgetPasswordStatus(String email,String status) {
		nativeRepository.updateQueries("update public.oauth_forget_password set status='"+status+"' where mail='"+email+"'");
	}
	
//	@Scheduled(fixedDelay = 1000)
	public void unAuhorizeForgetPassword() {
		nativeRepository.insertQueries("insert into public.oauth_forget_password_history (select * from public.oauth_forget_password where EXTRACT(EPOCH FROM (now() - createddate)) >600)");
	}
	

}
