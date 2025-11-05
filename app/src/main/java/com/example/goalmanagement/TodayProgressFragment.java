package com.example.goalmanagement;

import android.os.Bundle;
import androidx.annotation.NonNull; // Thêm import này
import androidx.annotation.Nullable; // Thêm import này
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Fragment để hiển thị tiến độ học tập trong ngày.
 */
public class TodayProgressFragment extends Fragment {

    // Khai báo các biến View và Adapter
    RecyclerView rvTodayTasks;
    TodayTaskAdapter taskAdapter; // Adapter này đã được tạo ở bước trước
    List<TodayTaskItem> taskItems; // Danh sách chứa dữ liệu các task
    TextView tvTodayDate;
    // TODO: Khai báo thêm các View khác nếu cần (ví dụ: ProgressBar, TextView streak...)

    // Constructor rỗng là bắt buộc
    public TodayProgressFragment() {}

    @Nullable // Đổi thành Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout (nạp giao diện) cho Fragment này
        View view = inflater.inflate(R.layout.fragment_today_progress, container, false);

        // Ánh xạ các View từ layout
        rvTodayTasks = view.findViewById(R.id.rv_today_tasks);
        tvTodayDate = view.findViewById(R.id.tv_today_date);
        // TODO: Ánh xạ các View khác (ProgressBar, TextView streak...)

        return view; // Trả về View đã được tạo
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Code xử lý logic sẽ đặt ở đây, sau khi View đã được tạo ---

        // 1. Hiển thị ngày hiện tại
        Calendar today = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        tvTodayDate.setText(dateFormat.format(today.getTime()));

        // 2. Load dữ liệu từ DB
        loadTaskData();

        // 3. Thiết lập RecyclerView và Adapter
        // Kiểm tra getContext() không null trước khi dùng
        if (getContext() != null && taskItems != null) {
            // Khởi tạo Adapter với context và danh sách dữ liệu
            taskAdapter = new TodayTaskAdapter(getContext(), taskItems);
            // Thiết lập LayoutManager và Adapter cho RecyclerView
            rvTodayTasks.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTodayTasks.setAdapter(taskAdapter);
        }

        // TODO: Cập nhật giao diện cho Streak, ProgressBar thống kê...
    }


    // Lấy dữ liệu thật cho hôm nay
    private void loadTaskData() {
        taskItems = new ArrayList<>();
        new Thread(() -> {
            try {
                com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(requireContext().getApplicationContext());
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String dayKey = sdf.format(new java.util.Date());
                java.util.List<com.example.goalmanagement.data.Task> tasks = db.taskDao().getByDay(dayKey);
                java.text.SimpleDateFormat hm = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                for (com.example.goalmanagement.data.Task t : tasks) {
                    String start = hm.format(new java.util.Date(t.startAtMillis));
                    String end = hm.format(new java.util.Date(t.endAtMillis));
                    String time = start + " - " + end;
                    String statusText;
                    switch (t.status) {
                        case "completed": statusText = "Hoàn thành"; break;
                        case "postponed": statusText = "Dời lịch"; break;
                        case "inprogress": statusText = "Đang học"; break;
                        case "pending": default: statusText = "Chưa bắt đầu"; break;
                    }
                    taskItems.add(new TodayTaskItem(t.title, time, statusText, t.status));
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (taskAdapter == null) {
                            taskAdapter = new TodayTaskAdapter(getContext(), taskItems);
                            rvTodayTasks.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
                            rvTodayTasks.setAdapter(taskAdapter);
                        } else {
                            taskAdapter.updateData(taskItems);
                        }
                    });
                }
            } catch (Exception ignored) { }
        }).start();
    }

    // --- PHẦN CLASS TodayTaskItem ĐÃ ĐƯỢC XÓA KHỎI ĐÂY ---

} // Kết thúc class TodayProgressFragment