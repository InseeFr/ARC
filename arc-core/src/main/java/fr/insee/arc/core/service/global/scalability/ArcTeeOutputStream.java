package fr.insee.arc.core.service.global.scalability;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ArcTeeOutputStream extends OutputStream{

	
	private List<OutputStream> branches = new ArrayList<>();
	

	public void add(OutputStream os)
	{
		branches.add(os);
	}

	public ArcTeeOutputStream() {
		super();
	}

	@Override
	public void write(int b) throws IOException {
		for (OutputStream branch:branches)
		{
			branch.write(b);
		}
	}
	
	@Override
    public void write(byte[] b) throws IOException {
		for (OutputStream branch:branches)
		{
			branch.write(b, 0, b.length);
		}
    }
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		for (OutputStream branch:branches)
		{
			branch.write(b, off,len);
		}
	 }
	
	@Override
	public void flush() throws IOException {
		for (OutputStream branch:branches)
		{
			branch.flush();
		}
	 }
	
	@Override
	public void close() throws IOException {
		for (OutputStream branch:branches)
		{
			branch.close();
		}
	 }

}
