/**
 * 
 */
package bixie.translation.jsonstubs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import bixie.translation.soot.SootStmtSwitch;
import bixie.util.Log;
import soot.Value;
import soot.jimple.InvokeExpr;

/**
 * @author schaef
 *
 */
public class JsonStubber {

	private final List<IStub> stubs;
	
	/**
	 * Load a set of stubs from json file
	 */
	public JsonStubber() {
		stubs = new ArrayList<IStub>();
	}

	public void loadStubs(File stubFile) {
		try (FileInputStream fis = new FileInputStream(stubFile)){
			loadStubs(fis);
		} catch (FileNotFoundException e) {
			Log.info("File not found "+stubFile);
		} catch (IOException e1) {
			Log.info("File not found "+stubFile);
		}
	}
	
	public void loadStubs(InputStream inputStream) {
		Gson gson = new Gson();	
		try (InputStreamReader isr= new InputStreamReader(inputStream, StandardCharsets.UTF_8)){
			JsonReader reader = new JsonReader(isr);
			java.lang.reflect.Type listType = new TypeToken<ArrayList<JsonStubDescriptor>>(){}.getType();
			List<JsonStubDescriptor> data = gson.fromJson(reader, listType);
			if (data!=null) {
				for (JsonStubDescriptor descr : data) {
					addStub(descr);
				}				
				Log.info("Imported "+data.size()+" stubs.");
			}
		} catch (JsonSyntaxException e) {
			Log.error("Cannot read stubs");
		} catch (Exception e) {
			Log.error("Cannot read stubs");
			e.printStackTrace(System.err);
		}
	}
	
	private IStub fromDescriptor(JsonStubDescriptor descr) {
		switch (descr.type) {
		case Comparison:
			return (new JsonCompareAssertionStub(descr));
		case Boolean:
			return (new JsonBooleanAssertionStub(descr));
		case NonNull:
			return (new JsonNonNullAssertionStub(descr));
		default:
			Log.error("Unknown type");
			break;					
		}
		throw new RuntimeException("Not implemented");
	}
	
	public void addStub(JsonStubDescriptor descr) {
		for (IStub stb : this.stubs) {
			if (stb.getDescriptor().equals(descr)) {
				//no need to add the same stub twice
				return;
			}
		}
		stubs.add(fromDescriptor(descr));
	}
	
	public void writeStubsToJson(File jsonFile) {
		Gson g = new Gson();
		List<JsonStubDescriptor> stubDescriptors = new ArrayList<JsonStubDescriptor>();
		for (IStub stub : this.stubs) {
			stubDescriptors.add(stub.getDescriptor());
		}
		final String jsonString = g.toJson(stubDescriptors);
		try ( OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8);
				PrintWriter out = new PrintWriter(outputStreamWriter) ) {
			out.println(jsonString);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}
	
	
	public boolean tryApplyStubs(SootStmtSwitch ss, Value lhs, InvokeExpr ivk) {
		for (IStub stub : stubs) {
			if (stub.tryApply(ss, lhs, ivk)) {
				return true;
			}
		}
		return false;
	}
}
