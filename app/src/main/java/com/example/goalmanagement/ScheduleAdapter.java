package com.example.goalmanagement;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// --- Class ScheduleItem (giữ nguyên) ---
class ScheduleItem {
    String startTime;
    String endTime;
    String title;
    String description;
    String type; // "study" | "rest"
    long taskId; // id trong DB (nếu có), -1 nếu dữ liệu demo

    public ScheduleItem(String startTime, String endTime, String title, String description, String type) {
        this(startTime, endTime, title, description, type, -1);
    }

    public ScheduleItem(String startTime, String endTime, String title, String description, String type, long taskId) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.description = description;
        this.type = type;
        this.taskId = taskId;
    }
}

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<ScheduleItem> scheduleItems;
    private Context context;
    private OnScheduleInteractionListener listener; // Biến lưu trữ listener

    /**
     * Interface để Activity lắng nghe sự kiện click sửa/xóa từ Adapter
     */
    public interface OnScheduleInteractionListener {
        void onEditClick(int position); // Hàm xử lý khi nhấn nút Sửa
        void onDeleteClick(int position); // Hàm xử lý khi nhấn nút Xóa
    }

    /**
     * Constructor đã được cập nhật để nhận listener
     */
    public ScheduleAdapter(Context context, List<ScheduleItem> scheduleItems, OnScheduleInteractionListener listener) {
        this.context = context;
        this.scheduleItems = scheduleItems;
        this.listener = listener; // Lưu listener được truyền vào
    }

    /**
     * Hàm cập nhật dữ liệu (giữ nguyên)
     */
    public void updateData(List<ScheduleItem> newItems) {
        if (newItems != null) {
            this.scheduleItems.clear();
            this.scheduleItems.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Lấy vị trí hiện tại một cách an toàn
        int currentPosition = holder.getAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) {
            return; // Bỏ qua nếu vị trí không hợp lệ (ví dụ: item vừa bị xóa)
        }
        ScheduleItem item = scheduleItems.get(currentPosition);


        holder.tvStartTime.setText(item.startTime);
        holder.tvEndTime.setText(item.endTime);
        holder.tvTitle.setText(item.title);
        holder.tvDescription.setText(item.description);

        // Đổi màu nền CardView (giữ nguyên)
        int backgroundColor;
        if ("study".equalsIgnoreCase(item.type)) {
            backgroundColor = ContextCompat.getColor(context, R.color.colorCardCreateGoal);
        } else {
            backgroundColor = ContextCompat.getColor(context, R.color.colorCardFindFriend);
        }
        holder.cardView.setCardBackgroundColor(backgroundColor);

        // Xử lý ẩn/hiện timeline lines (giữ nguyên)
        if (currentPosition == 0) {
            holder.lineAbove.setVisibility(View.INVISIBLE);
        } else {
            holder.lineAbove.setVisibility(View.VISIBLE);
        }
        if (currentPosition == scheduleItems.size() - 1) {
            holder.lineBelow.setVisibility(View.INVISIBLE);
        } else {
            holder.lineBelow.setVisibility(View.VISIBLE);
        }

        // --- XỬ LÝ SỰ KIỆN SỬA/XÓA (ĐÃ CẬP NHẬT) ---
        holder.btnEdit.setOnClickListener(v -> {
            // Kiểm tra listener và vị trí hợp lệ trước khi gọi
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onEditClick(holder.getAdapterPosition()); // Gọi hàm onEditClick của Activity
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            // Kiểm tra listener và vị trí hợp lệ trước khi gọi
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(holder.getAdapterPosition()); // Gọi hàm onDeleteClick của Activity
            }
        });

        // Nhấn vào item để bắt đầu học nếu là phiên học
        holder.itemView.setOnClickListener(v -> {
            if ("study".equalsIgnoreCase(item.type)) {
                android.content.Intent intent = new android.content.Intent(context, StudyModeActivity.class);
                intent.putExtra("TASK_NAME", item.title);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduleItems.size();
    }

    // --- Class ViewHolder (giữ nguyên) ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStartTime, tvEndTime, tvTitle, tvDescription;
        ImageView btnEdit, btnDelete;
        View lineAbove, lineBelow;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStartTime = itemView.findViewById(R.id.tv_schedule_time_start);
            tvEndTime = itemView.findViewById(R.id.tv_schedule_time_end);
            tvTitle = itemView.findViewById(R.id.tv_schedule_title);
            tvDescription = itemView.findViewById(R.id.tv_schedule_description);
            btnEdit = itemView.findViewById(R.id.btn_schedule_edit);
            btnDelete = itemView.findViewById(R.id.btn_schedule_delete);
            lineAbove = itemView.findViewById(R.id.timeline_line_above);
            lineBelow = itemView.findViewById(R.id.timeline_line_below);
            cardView = itemView.findViewById(R.id.card_schedule_item);
        }
    }
}