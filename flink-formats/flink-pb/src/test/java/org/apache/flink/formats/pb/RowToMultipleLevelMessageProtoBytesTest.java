package org.apache.flink.formats.pb;

import org.apache.flink.formats.pb.serialize.PbRowSerializationSchema;
import org.apache.flink.formats.pb.testproto.MultipleLevelMessageTest;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;

import junit.framework.TestCase;

public class RowToMultipleLevelMessageProtoBytesTest extends TestCase {
	public void testMultipleLevelMessage() throws Exception {
		RowData subSubRow = GenericRowData.of(1, 2L);
		RowData subRow = GenericRowData.of(subSubRow, false);
		RowData row = GenericRowData.of(1, 2L, false, subRow);

		RowType rowType = PbRowTypeInformation.generateRowType(MultipleLevelMessageTest.getDescriptor());
		row = ProtobufTestHelper.validateRow(row, rowType);

		PbRowSerializationSchema serializationSchema = new PbRowSerializationSchema(
			rowType,
			MultipleLevelMessageTest.class.getName());

		byte[] bytes = serializationSchema.serialize(row);

		MultipleLevelMessageTest test = MultipleLevelMessageTest.parseFrom(bytes);

		assertFalse(test.getD().getC());
		assertEquals(1, test.getD().getA().getA());
		assertEquals(2L, test.getD().getA().getB());
		assertEquals(1, test.getA());
	}

	public void testNull() throws Exception {
		RowData row = GenericRowData.of(1, 2L, false, null);
		byte[] bytes = ProtobufTestHelper.rowToPbBytes(row, MultipleLevelMessageTest.class);

		MultipleLevelMessageTest test = MultipleLevelMessageTest.parseFrom(bytes);

		MultipleLevelMessageTest.InnerMessageTest1 empty = MultipleLevelMessageTest.InnerMessageTest1
			.newBuilder()
			.build();
		assertEquals(empty, test.getD());
	}
}
