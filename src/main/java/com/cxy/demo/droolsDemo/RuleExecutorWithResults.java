/**
 * 
 */
package com.cxy.demo.droolsDemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.CommandFactory;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.impl.NativeQueryResultRow;

/**
 * @author cxy
 */
public class RuleExecutorWithResults {

    /**
     * @param args
     */
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	public static void main(String[] args) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("4test.drl", RuleExecutorWithResults.class), ResourceType.DRL);
        if (kbuilder.hasErrors()) {
            System.err.println(kbuilder.getErrors().toString());
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("aa", "1");
        map.put("bb", "6");
        map.put("cc", "4");
        
        List cmds = new ArrayList();
        cmds.add(CommandFactory.newInsert(map,"map"));
        cmds.add(CommandFactory.newFireAllRules());
        cmds.add(CommandFactory.newQuery("MemCachedItem", "list all MemCachedItems from working memory"));
        ExecutionResults results = ksession.execute(CommandFactory.newBatchExecution(cmds));
        QueryResults qr = (QueryResults) results.getValue("MemCachedItem");
        Iterator iterator = qr.iterator();
        while(iterator.hasNext()){
        	NativeQueryResultRow o = (NativeQueryResultRow)iterator.next();
        	System.out.println(o);
        }
        System.out.println(map);
    }
    
    

}
