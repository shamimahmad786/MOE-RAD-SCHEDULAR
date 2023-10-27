package MOERADSCHEDULAR.UTILITY;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class QueryResult {
	private List<Object> columnName;
	private List<Map<String, Object>> rowValue;
	private List<Object> columnDataType;
	public List<Object> getColumnName() {
		return columnName;
	}
	public void setColumnName(List<Object> columnName) {
		this.columnName = columnName;
	}
	public List<Map<String, Object>> getRowValue() {
		return rowValue;
	}
	public void setRowValue(List<Map<String, Object>> rowValue) {
		this.rowValue = rowValue;
	}

	public List<Object> getColumnDataType() {
		return columnDataType;
	}
	public void setColumnDataType(List<Object> columnDataType) {
		this.columnDataType = columnDataType;
	}
	@Override
	public String toString() {
		return "QueryResult [columnName=" + columnName + ", rowValue=" + rowValue + "]";
	}
}
