package com.cxy.demo.droolsDemo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.rule.MapBackedClassLoader;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.impl.NativeQueryResultRow;

import com.alibaba.fastjson.JSON;
import com.cxy.demo.droolsDemo.pojo.SimpleKV;

public class DroolsRuleExecutor {

	private String libDir;
	
	private MapBackedClassLoader classLoader;
	private KnowledgeBuilder kBuilder;
	private List<Command<?>> putCmds;
	private List<Command<?>> queryCmds;
	
	public DroolsRuleExecutor(String libDir) throws IOException {
		this.libDir = libDir;
		this.putCmds = new ArrayList<Command<?>>();
		this.queryCmds = new ArrayList<Command<?>>();
		this.classLoader = buildClassLoader();
		KnowledgeBuilderConfiguration conf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(null, this.classLoader);
        this.kBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(conf);
	}
	
	private ClassLoader getParentClassLoader() {
        ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
        if (parentClassLoader == null) {
            parentClassLoader = ClassLoader.getSystemClassLoader();
        }
        return parentClassLoader;
    }
	
	private MapBackedClassLoader getMapBackedClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<MapBackedClassLoader>() {
            public MapBackedClassLoader run() {
                return new MapBackedClassLoader(getParentClassLoader());
            }
        });
    }
	
	private List<JarInputStream> getJars() throws FileNotFoundException, IOException{
		List<JarInputStream> jars = new ArrayList<JarInputStream>();
		File path = new File(libDir);
		if(path.isDirectory()){
			for(File f : path.listFiles()){
				if(!f.getName().endsWith(".jar"))
					continue;
				JarInputStream localJarInputStream = new JarInputStream(new FileInputStream(f));
				jars.add(localJarInputStream);
			}
		}
		return jars;
	}
	
	private MapBackedClassLoader buildClassLoader() throws IOException {
        MapBackedClassLoader mapBackedClassLoader = getMapBackedClassLoader();
        for (JarInputStream jis : getJars()) {
            JarEntry entry = null;
            byte[] buf = new byte[1024];
            int len = 0;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (!entry.isDirectory() && !entry.getName().endsWith(".java")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    while ((len = jis.read(buf)) >= 0) {
                        out.write(buf, 0, len);
                    }
                    mapBackedClassLoader.addResource(entry.getName(), out.toByteArray());
                }
            }

        }
        return mapBackedClassLoader;
    }
	
	@SuppressWarnings("rawtypes")
	public DroolsRuleExecutor put(Object obj){
		if(obj == null)
			return this;
		if(putCmds == null)
			putCmds = new ArrayList<Command<?>>();
		if(obj instanceof Collection)
			putCmds.add(CommandFactory.newInsertElements((Collection)obj));
		else {
			putCmds.add(CommandFactory.newInsert(obj));
		}
		return this;
	}
	
	public DroolsRuleExecutor addResource(String resourceFile, ResourceType type) {
		this.kBuilder.add(ResourceFactory.newFileResource(resourceFile), type);
		if (this.kBuilder.hasErrors()) {
            System.err.println(this.kBuilder.getErrors().toString());
        }
		return this;
	}
	
	public DroolsRuleExecutor addQueryCmd(String identifier, String name) {
		this.queryCmds.add(CommandFactory.newQuery(identifier, name));
		return this;
	}
	
	@SuppressWarnings("rawtypes")
	public ExecutionResults fireRules(){
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
	    kbase.addKnowledgePackages(kBuilder.getKnowledgePackages());
	    StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
		List<Command> cmds = new ArrayList<Command>();
		cmds.addAll(putCmds);
		cmds.add(CommandFactory.newFireAllRules());
		cmds.addAll(queryCmds);
        ExecutionResults results = ksession.execute(CommandFactory.newBatchExecution(cmds));
        return results;
	}
	
	public static void main(String[] args) throws IOException {
		DroolsRuleExecutor executor = new DroolsRuleExecutor("src/main/resources/libs");
		executor.addResource(executor.getClass().getResource("/4test.drl").getPath(), ResourceType.DRL);
		executor.put(new SimpleKV("key", "a vary loooooooooong value"));
		executor.addQueryCmd("SimpleRisk", "list all risks from working memory");
		ExecutionResults results = executor.fireRules();
		QueryResults qr = (QueryResults) results.getValue("SimpleRisk");
        Iterator<?> iterator = qr.iterator();
        while(iterator.hasNext()){
        	NativeQueryResultRow o = (NativeQueryResultRow)iterator.next();
        	System.out.println(JSON.toJSONString(o.get("risk")));
        }
	}
}
