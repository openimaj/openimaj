package org.openimaj.text.nlp.language;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.io.wrappers.Readable2DArrayBinary;
import org.openimaj.io.wrappers.ReadableArrayBinary;
import org.openimaj.io.wrappers.Writeable2DArrayBinary;
import org.openimaj.io.wrappers.WriteableArrayBinary;

import Jama.Matrix;

class LanguageModel implements ReadWriteableBinary{
	Matrix naiveBayesPC; // N x 1 
	Matrix naiveBayesPTC; // N x M
	String[] naiveBayesClasses; // the language classes
	TIntObjectHashMap<int[]> tk_output; 
	int[] tk_nextmove;
	int naiveBayesNFeats;
	
	public LanguageModel(){}
	
	@SuppressWarnings("unchecked")
	public LanguageModel(Map<String,Object> languageModel){	
		List<Double> nb_pc_list = (List<Double>) languageModel.get("nb_pc");
		double[][] nb_pc_darr = new double[1][nb_pc_list.size()];
		int i = 0;
		for (double value : nb_pc_list) {
			nb_pc_darr[0][i++] = value;
		}
		naiveBayesPC = Matrix.constructWithCopy(nb_pc_darr);
		
		List<List<Double>> nb_ptc_list = (List<List<Double>>) languageModel.get("nb_ptc");
		double[][] nb_ptc_darr = new double[nb_ptc_list.size()][nb_ptc_list.get(0).size()];
		i = 0;
		for (List<Double> row: nb_ptc_list) {
			int j = 0;
			for(double val : row){
				nb_ptc_darr[i][j++] = val;
			}
			i++;
		}
		naiveBayesPTC = Matrix.constructWithCopy(nb_ptc_darr);
		
		this.naiveBayesNFeats = (naiveBayesPTC.getColumnDimension() * naiveBayesPTC.getRowDimension()) / naiveBayesPC.getColumnDimension();
		
		List<String> nb_classes_list = (List<String>)languageModel.get("nb_classes");
		naiveBayesClasses = nb_classes_list.toArray(new String[nb_classes_list.size()]);
		
		tk_output = new TIntObjectHashMap<int[]>();
		Map<String,List<Double>> tk_output_map = (Map<String, List<Double>>) languageModel.get("tk_outp");
		for (Entry<String,List<Double>> entry : tk_output_map .entrySet()) {
			i = 0;
			int[] entryArr = new int[entry.getValue().size()];
			for (double entryVal : entry.getValue()) {
				entryArr[i++] = (int) entryVal;
			}
			tk_output.put(Integer.parseInt(entry.getKey()),entryArr );
		}
		List<Double> tk_nextmove_list = (List<Double>) languageModel.get("tk_nextmove");
		tk_nextmove = new int[tk_nextmove_list.size()];
		i = 0;
		for (double val : tk_nextmove_list) {
			tk_nextmove[i++] = (int)val;
		}
	}

	@Override
	public void writeBinary(final DataOutput out) throws IOException {
		new Writeable2DArrayBinary(naiveBayesPC.getArray()).writeBinary(out);
		new Writeable2DArrayBinary(naiveBayesPTC.getArray()).writeBinary(out); 
		WriteableArrayBinary<String> stringWriter = new WriteableArrayBinary<String>(naiveBayesClasses) {
			@Override
			protected void writeValue(String v, DataOutput out) throws IOException {
				out.writeUTF(v);
			}
		};
		stringWriter.writeBinary(out);
		out.writeInt(tk_output.size());
		this.tk_output.forEachEntry(new TIntObjectProcedure<int[]>() {

			@Override
			public boolean execute(int key, int[] value) {
				try {
					out.writeInt(key);
					out.writeInt(value.length);
					for (int i : value) {
						out.writeInt(i);
					}
				} catch (IOException e) {
					return false;
				}
				return true;
			}
		});
		out.writeInt(this.tk_nextmove.length);
		for (int nextmove : this.tk_nextmove) {
			out.writeInt(nextmove);
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "LANGMODEL".getBytes();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		Readable2DArrayBinary matrixReader = new Readable2DArrayBinary(null);
		matrixReader.readBinary(in);
		naiveBayesPC = Matrix.constructWithCopy(matrixReader.value);
		
		matrixReader.readBinary(in);
		naiveBayesPTC = Matrix.constructWithCopy(matrixReader.value);
		
		this.naiveBayesNFeats = (naiveBayesPTC.getColumnDimension() * naiveBayesPTC.getRowDimension()) / naiveBayesPC.getColumnDimension();
		
		ReadableArrayBinary<String> readableClasses = new ReadableArrayBinary<String>(null){

			@Override
			protected String readValue(DataInput in) throws IOException {
				return in.readUTF();
			}

			@Override
			protected String[] createEmpty(int sz) throws IOException {
				return new String[sz];
			}
		};
		readableClasses.readBinary(in);
		this.naiveBayesClasses = readableClasses.value;
		
		int nTKOut = in.readInt();
		this.tk_output = new TIntObjectHashMap<int[]>(nTKOut);
		for (int i = 0; i < nTKOut; i++) {
			int key = in.readInt();
			int length = in.readInt();
			int[] data = new int[length];
			for (int j = 0; j < length; j++) {
				data[j] = in.readInt();
			}
			this.tk_output.put(key, data);
		}
		int nextMoveLength = in.readInt();
		this.tk_nextmove = new int[nextMoveLength];
		for (int i = 0; i < nextMoveLength; i++) {
			this.tk_nextmove[i] = in.readInt();
		}	
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof LanguageModel)) return false;
		final LanguageModel that = (LanguageModel) other;
		
		boolean equal = true;
		equal = Arrays.deepEquals(this.naiveBayesClasses, that.naiveBayesClasses); if(!equal) return false;
		equal = this.naiveBayesNFeats == that.naiveBayesNFeats; if(!equal) return false;
		equal = Arrays.deepEquals(this.naiveBayesPC.getArray(),that.naiveBayesPC.getArray()); if(!equal) return false;
		equal = Arrays.deepEquals(this.naiveBayesPTC.getArray(),that.naiveBayesPTC.getArray()); if(!equal) return false;
		equal = Arrays.equals(this.tk_nextmove,that.tk_nextmove); if(!equal) return false;
		equal = this.tk_output.forEachEntry(new TIntObjectProcedure<int[]>() {

			@Override
			public boolean execute(int key, int[] value) {
				return Arrays.equals(value, that.tk_output.get(key));
			}
		});if(!equal) return false;
		return equal;
	}
}