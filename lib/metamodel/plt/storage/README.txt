metro/lib/metamodel/plt/storage
===============================
Storage library
===============================
Abhijit Davare
9/6/04
===============================

template(T)
public interface storageinterface extends Port
{
	update void write(int offset, int bytes, T [] data); 
	update void write_one(int offset, T data);
	eval T [] read(int offset, int bytes);
	eval T read_one(int offset);
}

template(T)
public medium genericStorage implements storageinterface-<T>-
{
	public genericStorage(String n, int size_of_token, int size)
	public elaborate void load(int offset, T new_data)
	public update void write(int offset, int items, T [] new_data)
	public update void write_one(int offset, T new_data)
	public eval T [] read(int offset, int items)
	public eval T read_one(int offset)
}

The storage library is defines a media - genericStorage - that implements a 
one-dimensional data array. The size of the array is set in the constructor and 
the type of the array is specified in the declaration. (This is a template 
class) Access methods are provided through the storageinterface interface shown 
above. A method is also provided to load data into the array before running the 
model. 

The primary function of this media is to introduce an explicit mechanism for 
modeling key data elements in a model that need to be exposed for the purpose of 
mapping. The read() and write() methods of the interface are designed so that 
they can easily be mapped onto read and write services provided by 
architectures. 
