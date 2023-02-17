//Description:This connector is used to execute functions in Sap.
/**
 * Copyright (C), 2020-2022, Inossem Canada
 * Author: GuangLong Liu
 * Date: 2022/06/27
 *
 * Params：
 *     ashost: The address of sap server.
 *     sysnr: The sysnr of sap server.
 *     port: The port of sap server.
 *     language: The language of sap server.
 *     client: The client of sap server.
 *     username: The username of sap server.
 *     password: The password of sap server.
 *
 * Content Example：
 *    {
 *    }
 *
 * Result:
 *      {
 *
 *      }
 *
 * Others:
 * History:
 *   1. Date: 2022/06/27
 *      Author: GuangLong Liu
 *      Modification: First Version
 */

import com.fasterxml.jackson.databind.JsonNode
import com.inossem.pgoplus.camunda.BPMNBusinessException
import com.inossem.pgoplus.camunda.PGoPlusException
import com.inossem.pgoplus.camunda.RuntimeContext
import com.inossem.pgoplus.core.command.abapcommand.ABAPFunctionCommandParams
import com.inossem.pgoplus.core.commandbase.CommandParameter
import com.inossem.pgoplus.core.commandbase.CommandParams
import com.inossem.pgoplus.core.commandbase.IGCommand
import com.inossem.pgoplus.core.common.Try
import com.inossem.pgoplus.core.exception.IPasException
import com.inossem.pgoplus.core.service.DefaultRuntimeContext
import com.inossem.pgoplus.core.utils.sap.abap.*
import com.inossem.pgoplus.core.utils.sap.sapaccessor.DestinationProvider
import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoException
import com.sap.conn.jco.JCoFunction
import groovy.util.logging.Slf4j
import com.fasterxml.jackson.databind.ObjectMapper

@Slf4j
public class SapFunctionConnector implements IGCommand {

    protected String user;
    protected String passwd;
    protected String client;
    protected String language;
    protected String ashost;
    protected String sysnr;
    protected String port;

    // private final ABAPFunctionHelper helper;
    // private final ABAPFunctionManager manager;
    ABAPFunctionCommandParams param;

    @Override
    RuntimeContext execute(String content) throws BPMNBusinessException, PGoPlusException {
        try {
            DestinationProvider provider = new DestinationProvider(
                    user, passwd, client, language, ashost, sysnr, port
            );
            log.info("regist destination!");
            provider.registerDestination();

            Try<JCoDestination> retGetDestination = provider.getDestination();
            if (retGetDestination.isFailure()) {
                log.error("Exception occured when trying to get Sap Intance!");
                throw new PGoPlusException("Exception occurred when trying to get Sap Instance!");
            }

            JCoDestination jCoDestination = retGetDestination.successValue();
            ObjectMapper mapper = new ObjectMapper();
            ABAPFunctionHelper helper = new ABAPFunctionHelperImpl(mapper);
            ABAPFunctionParameter parameter = helper.createParameterFromString(content);
            String functionName = parameter.getFunctionName();

            log.info("Get Sap Function :{}", functionName);
            JCoFunction function = jCoDestination.getRepository().getFunction(functionName);
            ABAPFunction abapFunction = new ABAPFunctionImpl(jCoDestination, function);
            ABAPFunctionResult result = abapFunction.execute(parameter);
            JsonNode resultFunc = helper.functionResult2JsonNode(result);
            log.info("removing registered destination!");
            provider.unregisterDestination();
            String EV_RETURN = resultFunc.get("exportParameters").get("EV_RETURN").asText();
            resultFunc.put("EV_RETURN",EV_RETURN);
            DefaultRuntimeContext context = new DefaultRuntimeContext(resultFunc,null);
            return context;
        }
        catch (IOException | ABAPException | JCoException | Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            IPasException iPasException = new IPasException("0202002", e);
            throw new PGoPlusException(e);
        }
    }

    @Override
    String getName() {
        return "SapFunctionConnector";
    }

    @Override
    List<CommandParameter> getParamsKeys() {
        ArrayList<CommandParameter> keys = new ArrayList();
        keys.add(new CommandParameter("username"));
        keys.add(new CommandParameter("password"));
        keys.add(new CommandParameter("client"));
        keys.add(new CommandParameter("language"));
        keys.add(new CommandParameter("ashost"));
        keys.add(new CommandParameter("sysnr"));
        keys.add(new CommandParameter("port"));

        return keys;
    }

    @Override
    String getConnectorName() {
        return null
    }

    @Override
    void init(CommandParams params) {
        if (params.ValidParams(paramsKeys)) {
            user = params.getValue("username").successValue();
            passwd = params.getValue("password").successValue();
            client = params.getValue("client").successValue();
            language = params.getValue("language").successValue();
            ashost = params.getValue("ashost").successValue();
            sysnr = params.getValue("sysnr").successValue();
            port = params.getValue("port").successValue();
        } else {
            throw new PGoPlusException("Exception occured when trying to init SapFunctionConnector!");
        }
    }
}
