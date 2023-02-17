//Description:This connector is used to echo the input message.
/**
 * Copyright (C), 2020-2022, Inossem Canada
 * Author: GuangLong Liu
 * Date: 2022/07/28

 * Params：
 *
 * Content Example：
 *    {
 *       "UserName" : "user0001"
 *    }
 *
 * Result:
 * {
 *      "Content": "{
 *          "UserName" : "user0001"
 *      }",
 *      "Status": "Success"
 *  }
 *
 * Others:
 * History:
 *   1. Date: 2022/07/28
 *      Author: GuangLong Liu
 *      Modification: First Version
 */

package com.inossem.pgoplus.connectors.simplefile

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.inossem.pgoplus.camunda.BPMNBusinessException
import com.inossem.pgoplus.camunda.JsonObjectRuntimeContext
import com.inossem.pgoplus.camunda.PGoPlusException
import com.inossem.pgoplus.camunda.RuntimeContext
import com.inossem.pgoplus.core.commandbase.CommandParameter
import com.inossem.pgoplus.core.commandbase.CommandParams
import com.inossem.pgoplus.core.commandbase.IGCommand
import groovy.util.logging.Slf4j

@Grapes
@Slf4j
class CheckFlowReturn implements IGCommand {

    CheckFlowReturn() {
    }

    @Override
    String getName() {
        return "EchoConnector";
    }

    @Override
    List<CommandParameter> getParamsKeys() {
        return Arrays.asList(
        );
    }

    @Override
    String getConnectorName() {
        return null
    }

    @Override
    void init(CommandParams params) {
    }

    @Override
    RuntimeContext execute(String content) throws BPMNBusinessException, PGoPlusException {
        log.info("EchoCommand:" + content)
        ObjectMapper mapper = new ObjectMapper()
        def variable = mapper.readTree(content)
        int ret = 0;
        int ret1 = 1;
        if(variable.has("group1group1")){
            for(JsonNode array1 in variable.get("group1").asList()){
                String matae = array1.get("E2MARAM008").get("MSTAE").asText();
                String matav = array1.get("E2MARAM008").get("MSTAV").asText();
                String vkorg = "";
                String werks = "";
                if(!array1.has("E2MVKEM003")){
                    ret=1; // if field is is null or not exist
                    break;
                }
                for(JsonNode mvkem in array1.get("E2MVKEM003").asList()){
                    String vmsta = mvkem.get("VMSTA").asText();
                    log.info("vmsta" + vmsta);
                    vmsta.trim()
                    if(matae.trim()!=""||matav.trim()!=""||vmsta.trim()!=""){
                        ret = 1; // 三个字段任意一个有值流程退出;
                    }
                    else
                        ret = 0 // 流程继续
                    vkorg = mvkem.get("VKORG").asText();

                }
                if(!array1.has("group2")){
                    ret1=1;// if field is is null or not exist
                    break;
                }
                for(JsonNode array2 in array1.get("group2").asList()){
                    werks = array2.get("E2MARCM008").get("WERKS").asText();
                }
                if(vkorg.trim()=="4317"){
                    if(werks.trim()=="1070"||werks.trim()=="1072")
                        ret1 = 0; //流程继续
                    else
                        ret1 = 1; //流程退出
                }
                else if (vkorg.trim()=="4350"){
                    if(werks.trim()=="1150")
                        ret1 = 0; //流程继续
                    else
                        ret1 = 1; //流程退出
                }
                else if (vkorg.trim()=="6050"){
                    if(werks.trim()=="1150")
                        ret1 = 0; //流程继续
                    else
                        ret1 = 1; //流程退出
                }
                else
                    ret1 = 1;

            }  
        }
        
        log.info("ret="+ ret +"ret1="+ret1);
        Date date = new Date();
        //System.out.println(new Timestamp(date.getTime()));
        String fileName = date.getTime()+".dat"
        log.info("fileName="+fileName);
        ObjectMapper mapperRet = new ObjectMapper();
        ObjectNode result = mapperRet.createObjectNode();
        //result.put("Content", content);
        result.put("Status", "Success");
        result.put("state", ret|ret1);

        return new JsonObjectRuntimeContext(mapper.writeValueAsString(result));

    }
}
