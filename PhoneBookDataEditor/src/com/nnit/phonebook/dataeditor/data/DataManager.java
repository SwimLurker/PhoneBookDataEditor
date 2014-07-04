package com.nnit.phonebook.dataeditor.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.nnit.phonebook.dataeditor.data.PhoneBookItem;
import com.nnit.phonebook.dataeditor.db.DepartmentDAO;
import com.nnit.phonebook.dataeditor.db.MapDAO;
import com.nnit.phonebook.dataeditor.db.SeatDAO;

import android.graphics.Bitmap;
import android.os.Environment;

public class DataManager {
	private static DataManager _instance = null;
	public static final String PACKAGE_NAME = "com.nnit.phonebook.dataeditor";
	public static final String DATAPACKAGE_DIR = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/";

	public static final String DATA_PACKAGE_NAME = "data.zip";
	public static final String PHONEBOOK_DATA_FILENAME = "iNNIT.json";
	public static final String SEAT_DB_FILENAME = "iNNIT.db";
	public static final String PHOTO_DIR = "photos";
	public static final String MAP_DIR = "maps";
	public static final String FLAG_FILENAME = "last";
	public static final String TEMP_DIR = "tmp";

	private HashMap<String, PhoneBookItem> pbItems = null;
	private HashMap<String, String> photos = null;
	private HashMap<String, DepartmentInfo> departments = null;
	private HashMap<Integer, MapItem> mapItems = null;
	private HashMap<Integer, String> maps = null;
	private HashMap<String, SeatInfo> seats = null;

	private HashMap<String, SoftReference<Bitmap>> mapPhotoCache = null;

	private boolean dataLoaded = false;
	private boolean bModified = false;
	
	private DepartmentDAO deptDAO = null;
	private MapDAO mapDAO = null;
	private SeatDAO seatDAO = null;

	private DataManager() {
		pbItems = new HashMap<String, PhoneBookItem>();
		photos = new HashMap<String, String>();
		departments = new HashMap<String, DepartmentInfo>();
		mapItems = new HashMap<Integer, MapItem>();
		maps = new HashMap<Integer, String>();
		seats = new HashMap<String, SeatInfo>();
		deptDAO = new DepartmentDAO();
		mapDAO = new MapDAO();
		seatDAO = new SeatDAO();
		mapPhotoCache = new HashMap<String, SoftReference<Bitmap>>();
		bModified = false;
	}

	public static DataManager getInstance() {
		if (_instance == null) {
			_instance = new DataManager();
		}
		return _instance;
	}

	public boolean existData() {

		File file = new File(getFlagFileAbsolutePath());

		if (!file.exists()) {
			return false;
		}
		return true;
	}

	public boolean loadDataPackage() {
		try {
			loadData();
		} catch (Exception exp) {
			exp.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean loadDataPackage(String fullFileName) {
		// first unpack data package
		try {
			deleteDataFiles();
			unpackDataPackage(new FileInputStream(fullFileName));
			writeFlagFile();
			loadData();
		} catch (Exception exp) {
			exp.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean savePackageToDisk(String fullFileName) {
		ZipOutputStream zos = null;
		try {
			File outFile = new File(fullFileName);
			File srcFile = new File(DATAPACKAGE_DIR);
			zos = new ZipOutputStream(new FileOutputStream(outFile));
			if (srcFile.isFile()) {
				addToDataPackage(zos, srcFile, "");
			} else {
				File[] entries = srcFile.listFiles();
				for (int i = 0; i < entries.length; i++) {
					if (entries[i].isDirectory()
							&& entries[i].getName().equalsIgnoreCase(TEMP_DIR)) {
						continue;
					}
					if (entries[i].isFile()
							&& entries[i].getName().equalsIgnoreCase(
									FLAG_FILENAME)) {
						continue;
					}
					addToDataPackage(zos, entries[i], "");
				}
			}
		} catch (IOException exp) {
			exp.printStackTrace();
			return false;
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return true;
	}

	public String getPhoneBookDataFileAbsolutePath() {
		return DATAPACKAGE_DIR + PHONEBOOK_DATA_FILENAME;
	}

	public String getSeatDBFileAbsolutePath() {
		return DATAPACKAGE_DIR + SEAT_DB_FILENAME;
	}

	public String getPhotoDirAbsolutePath() {
		return DATAPACKAGE_DIR + PHOTO_DIR + File.separator;
	}

	public String getMapDirAbsolutePath() {
		return DATAPACKAGE_DIR + MAP_DIR + File.separator;
	}

	public String getFlagFileAbsolutePath() {
		return DATAPACKAGE_DIR + FLAG_FILENAME;
	}

	public String getTempDirAbsolutePath() {
		return DATAPACKAGE_DIR + TEMP_DIR + File.separator;
	}

	public List<PhoneBookItem> getPhoneBookItemList() {
		if (!dataLoaded) {
			try {
				loadData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		List<String> initialsList = new ArrayList<String>();
		initialsList.addAll(pbItems.keySet());

		Collections.sort(initialsList);

		for (String initial : initialsList) {
			PhoneBookItem pb = pbItems.get(initial);
			if (!pb.isDeleted()) {
				result.add(pb);
			}
		}
		return result;
	}

	public List<PhoneBookItem> getPhoneBookItemList(Filter[] filters) {
		if (!dataLoaded) {
			try {
				loadData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		List<String> initialsList = new ArrayList<String>();
		initialsList.addAll(pbItems.keySet());

		Collections.sort(initialsList);

		for (String initial : initialsList) {
			PhoneBookItem pb = pbItems.get(initial);
			if (!pb.isDeleted()) {
				boolean bMatch = true;
				if (filters != null && filters.length > 0) {
					for (Filter f : filters) {
						if (!f.match(pb)) {
							bMatch = false;
							break;
						}
					}
				}
				if (bMatch) {
					result.add(pb);
				}
			}
		}
		return result;
	}

	public List<MapItem> getMapItemList() {
		if (!dataLoaded) {
			try {
				loadData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<MapItem> result = new ArrayList<MapItem>();
		List<Integer> floorNoList = new ArrayList<Integer>();
		floorNoList.addAll(mapItems.keySet());

		Collections.sort(floorNoList);

		for (int floor : floorNoList) {
			MapItem map = mapItems.get(floor);
			if (!map.isDeleted()) {
				result.add(map);
			}
		}
		return result;
	}
	
	public List<Integer> getMapFloors(){
		if (!dataLoaded) {
			try {
				loadData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<Integer> floors = new ArrayList<Integer>();
		floors.addAll(mapItems.keySet());
		Collections.sort(floors);
		return floors;
	}

	public String getPhotoFilenameByInitials(String initials) {
		if (initials == null) {
			return null;
		}
		return photos.get(initials.toLowerCase());
	}

	public String getMapFilenameByFloor(Integer floor) {
		if (floor == null) {
			return null;
		}
		return maps.get(floor);
	}

	private void loadData() throws Exception {
		pbItems = readPhoneBookDataFromJsonFile();
		photos = loadPhotosInfo();
		departments = loadDepartmentsInfo();
		mapItems = loadMapsInfo();
		maps = loadMapImagesInfo();
		seats = loadSeatsInfo();
		dataLoaded = true;
		bModified = false;
	}

	private HashMap<String, DepartmentInfo> loadDepartmentsInfo() {
		HashMap<String, DepartmentInfo> result = new HashMap<String, DepartmentInfo>();
		List<DepartmentInfo> departments = deptDAO.getAllDepartments();
		if (departments != null) {
			for (DepartmentInfo di : departments) {
				result.put(di.getDepartmentNO(), di);
			}
		}
		return result;
	}

	private void loadPhotosInfo(File rootDir, HashMap<String, String> result)
			throws Exception {
		if (!rootDir.exists() || (!rootDir.isDirectory())) {
			throw new Exception("Photo dir not exist");
		}

		File[] subFiles = rootDir.listFiles();
		for (File f : subFiles) {
			if (f.isDirectory()) {
				loadPhotosInfo(f, result);
			} else if (f.isFile()) {
				String initials = getInitilas(f);
				if (initials != null) {
					result.put(initials.toLowerCase(), f.getAbsolutePath());
				}
			}
		}
	}

	private HashMap<String, String> loadPhotosInfo() throws Exception {
		HashMap<String, String> result = new HashMap<String, String>();
		loadPhotosInfo(new File(getPhotoDirAbsolutePath()), result);
		return result;
	}

	private HashMap<Integer, MapItem> loadMapsInfo() {
		HashMap<Integer, MapItem> result = new HashMap<Integer, MapItem>();
		
		List<MapItem> maps = mapDAO.getAllMaps();
		if (maps != null) {
			for (MapItem mi : maps) {
				result.put(mi.getFloor(), mi);
			}
		}
		return result;
	}

	private HashMap<Integer, String> loadMapImagesInfo() throws Exception {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		Collection<MapItem> maps = mapItems.values();
		for (MapItem mi : maps) {
			result.put(mi.getFloor(),
					getMapDirAbsolutePath() + mi.getMapFilename());
		}
		return result;
	}
	
	private HashMap<String, SeatInfo> loadSeatsInfo() {
		HashMap<String, SeatInfo> result = new HashMap<String, SeatInfo>();
		List<SeatInfo> seats = seatDAO.getAllSeats();
		if (seats != null) {
			for (SeatInfo si : seats) {
				result.put(si.getInitials(), si);
			}
		}
		return result;
	}

	private String getInitilas(File file) {
		int pos = -1;
		String filename = file.getName();
		if ((pos = filename.lastIndexOf(".")) != -1) {
			String initials = filename.substring(0, pos);
			return initials;
		} else {
			return filename;
		}

	}

	private void deleteDataFiles() {
		File f = new File(DATAPACKAGE_DIR);
		if (f.exists() && f.isDirectory()) {
			File[] files2 = f.listFiles();
			for (File sf : files2) {
				delete(sf);
			}
		}

	}

	private void delete(File f) {
		if (f.isFile()) {
			f.delete();
		} else if (f.isDirectory()) {
			File[] childFiles = f.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				f.delete();
			} else {
				for (File ff : childFiles) {
					delete(ff);
				}
				f.delete();
			}

		}
	}

	private HashMap<String, PhoneBookItem> readPhoneBookDataFromJsonFile()
			throws Exception {

		HashMap<String, PhoneBookItem> result = new HashMap<String, PhoneBookItem>();

		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(new FileReader(
				getPhoneBookDataFileAbsolutePath()));
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}

		String text = sb.toString();
		if (text != null & text.startsWith("\ufeff")) {
			text = text.substring(1);
		}
		JSONObject object = new JSONObject(text);
		for (int i = 0; i < 26; i++) {
			char c = (char) ((int) 'A' + i);
			if (object.has(Character.toString(c))) {
				JSONArray array = object.getJSONArray(Character.toString(c));
				int len = array.length();
				for (int j = 0; j < len; j++) {
					JSONObject obj = array.getJSONObject(j);
					String initials = obj.getString("Initials");
					String name = obj.getString("Name");
					String localName = obj.getString("LocalName");
					String gender = obj.getString("Gender");
					String phone = obj.getString("Phone");
					String departmentNo = obj.getString("DepartmentNO");
					String department = obj.getString("Department");
					String title = obj.getString("Title");
					String manager = obj.getString("Manager");

					PhoneBookItem item = new PhoneBookItem();
					item.setInitials(initials);
					item.setName(name);
					item.setLocalName(localName);
					if ("MALE".equalsIgnoreCase(gender)) {
						item.setGender(PhoneBookItem.GENDER.MALE);
					} else if ("FEMALE".equalsIgnoreCase(gender)) {
						item.setGender(PhoneBookItem.GENDER.FEMALE);
					} else {
						item.setGender(PhoneBookItem.GENDER.UNKNOWN);
					}
					item.setPhone(phone);
					item.setDepartmentNo(departmentNo);
					item.setDepartment(department);
					item.setTitle(title);
					item.setManager(manager);
					result.put(initials, item);
				}
			}
		}

		return result;

	}
	
	private void savePhoneBookDataToJsonFile() {
		FileOutputStream fos = null;
		try {
			String filename = getPhoneBookDataFileAbsolutePath();
			File f = new File(filename);
			if(f.exists()){
				f.delete();
			}
			
			fos = new FileOutputStream(filename);

			fos.write(serializePhoneBookItems(pbItems).getBytes());
			
		} catch (Exception exp) {
			exp.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
				fos = null;
			}
		}
	}
	private String serializePhoneBookItems(HashMap<String, PhoneBookItem> pbis) {
	
		StringBuffer sb = new StringBuffer();
		List<String> initialsList = new ArrayList<String>();
		initialsList.addAll(pbis.keySet());
		Collections.sort(initialsList);
		
		String firstChar = null;
		String lastFirstChar = null;
		int num = initialsList.size();
		for (int i = 0; i< num; i++) {
			String initials = initialsList.get(i);
			PhoneBookItem pbi = pbItems.get(initials);
			if(pbi.isDeleted()){
				continue;
			}
			firstChar = initials.substring(0, 1).toUpperCase();
			if(lastFirstChar == null){
				sb.append("{\r\n");
				sb.append("\t\"");
				sb.append(firstChar);
				sb.append("\":[\r\n");
			}else if(!lastFirstChar.equalsIgnoreCase(firstChar)){
					sb.append("\r\n\t],\"");
					sb.append(firstChar);
					sb.append("\":[\r\n");
			}else{
				sb.append(",\r\n");
			}
			sb.append("\t\t");
			sb.append(pbi.toJSONString());
			
			lastFirstChar = firstChar;
			
		}
		sb.append("\r\n\t]\r\n}");

		return sb.toString();
	}

	private void writeFlagFile() throws IOException {
		File flagFile = new File(getFlagFileAbsolutePath());
		if (!flagFile.exists()) {
			flagFile.createNewFile();
		}

		FileWriter fw = new FileWriter(flagFile);
		fw.write(new Date().toString());
		fw.close();

	}

	private void unpackDataPackage(InputStream dataPackageIS)
			throws IOException {

		File file = new File(DATAPACKAGE_DIR);

		if (!file.exists()) {
			file.mkdirs();
		}
		ZipInputStream zis = new ZipInputStream(dataPackageIS);

		ZipEntry zipEntry = zis.getNextEntry();

		byte[] buf = new byte[1024 * 32];

		int count = 0;
		while (zipEntry != null) {

			file = new File(DATAPACKAGE_DIR + zipEntry.getName());
			if (zipEntry.isDirectory()) {
				file.mkdirs();
			} else {
				int index = zipEntry.getName().lastIndexOf("/");
				if(index != -1){
					File df = new File(DATAPACKAGE_DIR + zipEntry.getName().substring(0, index));
					df.mkdirs();
				}
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				while ((count = zis.read(buf)) > 0) {
					fos.write(buf, 0, count);
				}
				fos.close();
			}

			zipEntry = zis.getNextEntry();
		}
		zis.close();
	}

	private void addToDataPackage(ZipOutputStream zos, File file, String curPath)
			throws IOException {
		FileInputStream fis = null;
		try {
			if (!file.isDirectory()) {
				byte[] buf = new byte[4096];
				int count = 0;
				fis = new FileInputStream(file);
				ZipEntry entry = new ZipEntry(curPath + file.getName());
				zos.putNextEntry(entry);
				while ((count = fis.read(buf)) != -1) {
					zos.write(buf, 0, count);
				}
				zos.closeEntry();
			} else {
				File[] entries = file.listFiles();
				for (File f : entries) {
					addToDataPackage(zos, f, curPath + file.getName() + "/");
				}
			}
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void setPhoneBookDataDeleted(Set<String> initialsSet) {
		boolean bSet = false;

		for (String initials : initialsSet) {
			if (pbItems.containsKey(initials)) {
				pbItems.get(initials).setFlag(FLAG.DELETED);
				SeatInfo si = seats.get(initials);
				if(si != null){
					si.setFlag(FLAG.DELETED);
				}
				bSet = true;
			}
		}
		if (bSet) {
			bModified = true;
		}
	}

	public void setMapDataDeleted(Set<Integer> floors) {
		boolean bSet = false;

		for (Integer floor : floors) {
			if (mapItems.containsKey(floor)) {
				mapItems.get(floor).setFlag(FLAG.DELETED);
				bSet = true;
			}
		}
		if (bSet) {
			bModified = true;
		}

	}

	public boolean isDataModified() {
		return bModified;
	}

	public boolean saveModification() {
		try{
			savePhoneBookDataToJsonFile();
			savePhotosInfo();
			saveMapsInfo();
			saveMapImagesInfo();
			saveSeatInfo();
			
			clearTempData();
			
			bModified = false;
		}catch(Exception exp){
			exp.printStackTrace();
			return false;
		}
		return true;
		
	}

	private void clearTempData() {
		File f = new File(getTempDirAbsolutePath());
		if(f.exists()){
			delete(f);
		}
	}

	private void saveSeatInfo() {
		Collection<SeatInfo> sis = seats.values();
		for(SeatInfo si:sis){
			String initials = si.getInitials();
			PhoneBookItem pbi = pbItems.get(initials);
			if(pbi == null) continue;
			if(si.isDeleted() || pbi.isDeleted()){
				seatDAO.deleteSeatByInitials(initials);
			}else if(si.isNewCreated()||si.isModified()){
				seatDAO.insertOrUpdateSeatInfo(si);
			}
		}
		
	}

	private void saveMapImagesInfo() {
		Collection<MapItem> mis = mapItems.values();
		for(MapItem mi:mis){			
			if(mi.isDeleted()){
				deleteMapFileByFloor(mi.getFloor()); 
			}else if(mi.isNewCreated()||mi.isModified()){
				modifyMapFileByFloor(mi.getFloor());
			}
		}
		
	}

	private void modifyMapFileByFloor(int floor) {
		String mapFilename = maps.get(floor);
		if(mapFilename != null){
			File f = new File(mapFilename);
			String newFilename = getMapDirAbsolutePath() + f.getName();
			
			File newF = new File(newFilename);
			if(newF.exists()){
				newF.delete();
			}
			f.renameTo(newF);
			maps.put(floor, newFilename);

		}
		
	}

	private void deleteMapFileByFloor(int floor) {
		String mapFilename = maps.get(floor);
		if(mapFilename != null){
			File f = new File(mapFilename);
			if(f.exists()){
				f.delete();
			}
		}
		maps.remove(floor);
	}

	private void saveMapsInfo() {
		Collection<MapItem> mis = mapItems.values();
		for(MapItem mi:mis){
			if(mi.isDeleted()){
				mapDAO.deleteMapByFloorNo(mi.getFloor());
			}else if(mi.isNewCreated()||mi.isModified()){
				mapDAO.insertOrUpdateMap(mi);
			}
		}
	}

	private void savePhotosInfo() throws IOException{
		Collection<PhoneBookItem> pbis = pbItems.values();
		
		for(PhoneBookItem pbi: pbis){
			if(pbi.isDeleted()){
				deletePhotoFileByInitials(pbi.getInitials());
			}else if(pbi.isNewCreated()||pbi.isModified()){
				modifyPhotoFileByInitials(pbi.getInitials());
			}
		}

	}

	private void modifyPhotoFileByInitials(String initials) throws IOException{
		String photosFilename = photos.get(initials.toLowerCase());
		if(photosFilename != null && photosFilename.startsWith(getPhotoDirAbsolutePath())){
			//photo not changed, do nothing
		}else if(photosFilename != null && photosFilename.startsWith(getTempDirAbsolutePath())){
			//copy file to photo dir
			String newFilename = getPhotoDirAbsolutePath() + initials.toUpperCase() + ".png";
			File f = new File(newFilename);
			if(f.exists()){
				f.delete();
			}
			File modifiedFile = new File(photosFilename);
			if(!modifiedFile.renameTo(f)){
				throw new IOException("Rename photo file exception: from:" + photosFilename + ", to:" + newFilename); 
			}
		}
		
	}

	private void deletePhotoFileByInitials(String initials) {
		String originalFilename = photos.get(initials.toLowerCase());
		if(originalFilename != null){
			File f = new File(originalFilename);
			if(f.exists()){
				f.delete();
			}
		}
	}

	public List<DepartmentInfo> getAllDepartments() {
		if (!dataLoaded) {
			try {
				loadData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<DepartmentInfo> result = new ArrayList<DepartmentInfo>();
		List<String> depNOs = new ArrayList<String>();
		depNOs.addAll(departments.keySet());
		Collections.sort(depNOs);
		for(String depNO: depNOs){
			result.add(departments.get(depNO));
		}
		return result;
	}

	public boolean newPhoneBook(PhoneBookItem newPbItem) {
		if (pbItems.containsKey(newPbItem.getInitials())) {
			PhoneBookItem pbi = pbItems.get(newPbItem.getInitials());
			setPBItemValue(pbi, newPbItem);
		} else {
			pbItems.put(newPbItem.getInitials(), newPbItem);
		}

		bModified = true;
		return true;
	}

	public boolean newMapInfo(MapItem newMapItem) {
		if (mapItems.containsKey(newMapItem.getFloor())) {
			MapItem mi = mapItems.get(newMapItem.getFloor());
			setMapItemValue(mi, newMapItem);
		} else {
			mapItems.put(newMapItem.getFloor(), newMapItem);
		}

		bModified = true;
		return true;
	}

	public boolean updatePhoneBook(String initials, PhoneBookItem newPbItem) {
		boolean bUpdated = false;
		if (pbItems.containsKey(initials)) {
			PhoneBookItem pbi = pbItems.get(initials);
			setPBItemValue(pbi, newPbItem);
			bUpdated = true;
		}

		if (bUpdated) {
			bModified = true;
		}
		return bUpdated;
	}

	private void setPBItemValue(PhoneBookItem pbi, PhoneBookItem newPbItem) {
		pbi.setName(newPbItem.getName());
		pbi.setLocalName(newPbItem.getLocalName());
		pbi.setDepartment(newPbItem.getDepartment());
		pbi.setDepartmentNo(newPbItem.getDepartmentNo());
		pbi.setGender(newPbItem.getGender());
		pbi.setManager(newPbItem.getManager());
		pbi.setPhone(newPbItem.getPhone());
		pbi.setTitle(newPbItem.getTitle());
		pbi.setFlag(FLAG.MODIFIED);
	}

	private void setMapItemValue(MapItem mi, MapItem newMi) {
		mi.setMapFilename(newMi.getMapFilename());
		mi.setFlag(FLAG.MODIFIED);
	}

	public boolean newPhoneBookPhoto(String initials, Bitmap newPhoto) {
		boolean bUpdated = false;
		if (newPhoto != null) {
			String newPhotoFilename = initials.toUpperCase() + ".png";
			if (saveBitmapToFile(newPhoto,
					getTempDirAbsolutePath() + "photos/", newPhotoFilename)) {
				photos.put(initials.toLowerCase(), getTempDirAbsolutePath()
						+ "photos/" + newPhotoFilename);
				bUpdated = true;
			} else {
				bUpdated = false;
			}

		}
		if (bUpdated) {
			bModified = true;
		}
		return bUpdated;
	}

	public boolean newMapImage(MapItem map, Bitmap newMap) {
		boolean bUpdated = false;
		if (newMap != null) {
			String newMapPath = getTempDirAbsolutePath() + "maps/";
			String newMapFilename = newMapPath + map.getMapFilename();
			if (saveBitmapToFile(newMap, newMapPath, map.getMapFilename())) {
				maps.put(map.getFloor(), newMapFilename);
				bUpdated = true;
			} else {
				bUpdated = false;
			}
		}
		if (bUpdated) {
			bModified = true;
		}
		return bUpdated;
	}

	public boolean updatePhoneBookPhoto(String initials, Bitmap newPhoto) {
		boolean bUpdated = false;
		if (newPhoto != null) {
			String newPhotoFilename = initials.toUpperCase() + ".png";
			if (saveBitmapToFile(newPhoto,
					getTempDirAbsolutePath() + "photos/", newPhotoFilename)) {
				photos.put(initials.toLowerCase(), getTempDirAbsolutePath()
						+ "photos/" + newPhotoFilename);
				bUpdated = true;
			} else {
				bUpdated = false;
			}

		}
		if (bUpdated) {
			bModified = true;
		}
		return bUpdated;
	}

	private boolean saveBitmapToFile(Bitmap bitmap, String path, String filename) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File f = new File(dir, filename);
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream fos = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
			fos.flush();
			fos.close();
		} catch (Exception exp) {
			exp.printStackTrace();
			return false;
		}
		return true;
	}

	public void updateMapThumbnailPhotoCache(String mapFilename, Bitmap bitmap) {
		mapPhotoCache.put(mapFilename, new SoftReference<Bitmap>(bitmap));

	}

	public Bitmap getMapThumbnailPhotoFromCache(String mapFilename) {
		if (mapPhotoCache.containsKey(mapFilename)) {
			return mapPhotoCache.get(mapFilename).get();
		}
		return null;
	}

	public void removeMapThumbnailPhotoFromCache(String mapFilename) {
		mapPhotoCache.remove(mapFilename);

	}

	public SeatInfo getSeatInfoByInitial(String initials) {
		if (!dataLoaded) {
			try {
				loadData();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return seats.get(initials);
	}

	
	public boolean updateSeatInfo(String initials, SeatInfo newSeatInfo) {
		boolean bUpdated = false;
		if (seats.containsKey(initials)) {
			SeatInfo si = seats.get(initials);
			setSeatInfoValue(si, newSeatInfo);
			bUpdated = true;
		}else{
			newSeatInfo.setFlag(FLAG.NEW);
			seats.put(initials, newSeatInfo);
		}

		if (bUpdated) {
			bModified = true;
		}
		return bUpdated;
	}

	private void setSeatInfoValue(SeatInfo si, SeatInfo newSeatInfo) {
		si.setFloorNo(newSeatInfo.getFloorNo());
		si.setX(newSeatInfo.getX());
		si.setY(newSeatInfo.getY());
		si.setWidth(newSeatInfo.getWidth());
		si.setHeight(newSeatInfo.getHeight());
		si.setDirection(newSeatInfo.getDirection());
		si.setFlag(FLAG.MODIFIED);	
	}

}
